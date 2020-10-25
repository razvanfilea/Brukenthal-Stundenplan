import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import fetch from 'node-fetch';
import {JSDOM} from 'jsdom';

admin.initializeApp();

const SITE_URL = "https://brukenthal.ro";

const KEY_HIGH_SCHOOL = "url_high_school";
const KEY_MIDDLE_SCHOOL = "url_middle_school";

const CHANNEL_ID_DEFAULT = "default";
const CHANNEL_ID_HIGH_SCHOOL = "high_school";
const CHANNEL_ID_MIDDLE_SCHOOL = "middle_school";

class ConfigValues {
    readonly highSchool: string
    readonly middleSchool: string

    constructor(highSchoolValue: string, middleSchoolValue: string) {
        this.highSchool = highSchoolValue;
        this.middleSchool = middleSchoolValue;
    }
}

function processRemoteConfigTemplate(template: admin.remoteConfig.RemoteConfigTemplate): ConfigValues {
    const parameters = template.parameters;

    const highSchoolDefault = parameters[KEY_HIGH_SCHOOL].defaultValue as
        admin.remoteConfig.ExplicitParameterValue;
    const middleSchoolDefault = parameters[KEY_MIDDLE_SCHOOL].defaultValue as
        admin.remoteConfig.ExplicitParameterValue;

    return new ConfigValues(highSchoolDefault.value, middleSchoolDefault.value);
}

function randomInt(min: number, max: number): number {
    return Math.floor(Math.random() * (max - min + 1) + min);
}

async function updateRemoteConfig(newMiddleSchoolUrl: string, newHighSchoolUrl: string): Promise<void> {
    console.log("High School Url: " + newHighSchoolUrl);
    console.log("Middle School Url: " + newMiddleSchoolUrl);

    const config = admin.remoteConfig();
    const template = await config.getTemplate();

    const values = processRemoteConfigTemplate(template);

    // Check if the values have changed
    if (values.highSchool !== newHighSchoolUrl || values.middleSchool !== newMiddleSchoolUrl) {
        // Update the template
        template.parameters[KEY_HIGH_SCHOOL] = {
            defaultValue: {value: newHighSchoolUrl}
        };
        template.parameters[KEY_MIDDLE_SCHOOL] = {
            defaultValue: {value: newMiddleSchoolUrl}
        };

        // Publish the updated template
        return config.publishTemplate(template)
            .then(() => {
                console.log("Template has been published");
            })
            .catch(err => {
                console.error("Unable to publish template.");
                console.error(err);
            });
    }

    return Promise.resolve();
}

exports.checkForNewTimetable = functions
    .region('europe-west1')
    .pubsub
    .schedule('every 20 minutes')
    .onRun(async () => {
        const text = await fetch(SITE_URL).then(data => data.text())

        const dom = new JSDOM(text);
        const doc = dom.window.document;

        // Parse the HTML and get the urls
        const middleSchoolUrl =
            doc.querySelector("li.menu-item-1320 a")!.getAttribute("href");
        const highSchoolUrl =
            doc.querySelector("li.menu-item-1470 a")!.getAttribute("href");

        return updateRemoteConfig(SITE_URL + middleSchoolUrl, SITE_URL + highSchoolUrl);
    });

const TITLES = [
    "Der Stundenplan hat sich verändert!",
    "Ein neuer Stundenplan ist da!",
    "Achtung Stundenplanveränderung!",
    "Der Stundenplan ist aktualisiert!",
    "Neue Veränderungen im Stundenplan!"
];
const MESSAGES = [
    "Yeah, I've got time",
    "Happy Spooktober🎃",
    "Are ya winning son?",
    "Kowalski, Analysis!",
    "Stay safe!",
    "Hello There",
    "You cannot handle the true power of Spinjitzu",
    "Your timetable is temporary but Doom is Eternal",
    "Hey, you, you're finally awake"
];

exports.sendNewTimetableNotification = functions
    .region('europe-west1')
    .remoteConfig
    .onUpdate(async (versionMetadata) => {
        const config = admin.remoteConfig();
        const newTemplate = config.getTemplate();
        const oldTemplate = config.getTemplateAtVersion(versionMetadata.versionNumber - 1);

        const selectedTitle = TITLES[randomInt(0, TITLES.length - 1)];
        const selectedMessage = MESSAGES[randomInt(0, MESSAGES.length - 1)];

        const newValues = processRemoteConfigTemplate(await newTemplate);
        const oldValues = processRemoteConfigTemplate(await oldTemplate);

        let preTitle = "";
        let channel = CHANNEL_ID_DEFAULT;

        if (oldValues.middleSchool === newValues.middleSchool && oldValues.highSchool !== newValues.highSchool) {
            preTitle = "Lyzeum: ";
            channel = CHANNEL_ID_HIGH_SCHOOL;
        } else if (oldValues.middleSchool !== newValues.middleSchool && oldValues.highSchool === newValues.highSchool) {
            preTitle = "Gymnasium: ";
            channel = CHANNEL_ID_MIDDLE_SCHOOL;
        } else if (oldValues.middleSchool === newValues.middleSchool && oldValues.highSchool === newValues.highSchool)
            return

        const payload = {
            data: {
                title: preTitle + selectedTitle,
                body: selectedMessage,
                channel_id: channel
            }
        };

        return admin.messaging().sendToCondition("\'all\' in topics", payload).then(_ =>
            console.log("Notification sent")
        ).catch(err => {
            console.error("Notification failed to send");
            console.error(err)
        });
    });
