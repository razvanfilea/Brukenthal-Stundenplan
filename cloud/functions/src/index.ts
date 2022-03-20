import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import fetch from "node-fetch";
// import fetch from "node-fetch";
import {JSDOM} from 'jsdom';

admin.initializeApp();

/// Constants

const SITE_URL = "https://brukenthal.ro";

/**
 * The keys defined in the Firebase Console -> Remote Config,
 * used to access and store the timetable URLs
 */
const KEY_HIGH_SCHOOL = "url_high_school";
const KEY_MIDDLE_SCHOOL = "url_middle_school";

/**
 * Channel IDs used for sending notifications
 * This need to be the same as in the 'NotificationService' Kotlin class
 */
const CHANNEL_ID_DEFAULT = "notifications";
const CHANNEL_ID_HIGH_SCHOOL = "high_school";
const CHANNEL_ID_MIDDLE_SCHOOL = "middle_school";

/**
 * The titles and messages used for notifications
 */
const TITLES = [
    "Der Stundenplan wurde geändert!",
    "Ein neuer Stundenplan ist da",
    "Achtung, neuer Stundenplan!",
    "Achtung, Stundenplanänderung!",
    "Stundenplan erneut geändert!",
    "Der Stundenplan ist aktualisiert",
    "Ein neuer Stundenplan wurde hochgeladen."
];
const MESSAGES = [
    "Creeper! Aww man!",
    "Some people call this junk. Me? I call it treasure",
    "Geschwindigkeit und Präzision!",
    "The risk I took was calculated but man am I bad at math",
    "Minim de efort maxim de eficiență",
    "Haide mai bine, să nu",
    "Ani trec robotica rămâne",
    "No I don't think I will",
    "One does not simply walk into Bruk."
];

/// Structures

/**
 * A small immutable class used for convenience to store the links to the timetables
 */
class ConfigValues {
    readonly highSchool: string
    readonly middleSchool: string

    constructor(highSchoolValue: string, middleSchoolValue: string) {
        this.highSchool = highSchoolValue;
        this.middleSchool = middleSchoolValue;
    }
}

/// Helper Functions

/**
 * Self explanatory, generate a random integer between [min] and [max]
 */
function randomInt(min: number, max: number): number {
    return Math.floor(Math.random() * (max - min + 1) + min);
}

/**
 * Parses a RemoteConfig template into a ConfigValues
 * @param template the template to parse
 */
function processRemoteConfigTemplate(template: admin.remoteConfig.RemoteConfigTemplate): ConfigValues {
    const parameters = template.parameters;

    const highSchoolDefault = parameters[KEY_HIGH_SCHOOL].defaultValue as
        admin.remoteConfig.ExplicitParameterValue;
    const middleSchoolDefault = parameters[KEY_MIDDLE_SCHOOL].defaultValue as
        admin.remoteConfig.ExplicitParameterValue;

    return new ConfigValues(highSchoolDefault.value, middleSchoolDefault.value);
}

/**
 * Updates the Remote Config values
 */
async function updateRemoteConfig(newConfigValues: ConfigValues): Promise<void> {
    console.log("High School Url: " + newConfigValues.highSchool);
    console.log("Middle School Url: " + newConfigValues.middleSchool);

    const config = admin.remoteConfig(); // Get Access to Firebase Remote Config
    const template = await config.getTemplate(); // Get the current template

    const currentValues = processRemoteConfigTemplate(template);

    // Check if the currentValues have changed
    if (currentValues.highSchool !== newConfigValues.highSchool
        || currentValues.middleSchool !== newConfigValues.middleSchool) {

        // Update the template
        template.parameters[KEY_HIGH_SCHOOL] = {
            defaultValue: {value: newConfigValues.highSchool}
        };
        template.parameters[KEY_MIDDLE_SCHOOL] = {
            defaultValue: {value: newConfigValues.middleSchool}
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

    // There's nothing to do so return an empty Promise
    return Promise.resolve();
}

/// Firebase Functions

/**
 * Register the Scheduled Cloud Function
 */
exports.checkForNewTimetable = functions
    .region('europe-west1')
    .pubsub
    .schedule('every 50 minutes')
    .onRun(async () => {
        // Fetch and store the site as HTML
        const html = await fetch(SITE_URL).then(data => data.text())

        // Parse the HTML
        const dom = new JSDOM(html);
        const doc = dom.window.document;

        // Get the urls
        // TODO: This part has to be changed if the website is changed!!!
        const middleSchoolUrl =
            doc.querySelector("li.menu-item-1320 a")!.getAttribute("href");
        const highSchoolUrl =
            doc.querySelector("li.menu-item-1470 a")!.getAttribute("href");

        const newConfigValues = new ConfigValues(
            SITE_URL + highSchoolUrl,
            SITE_URL + middleSchoolUrl
        );
        return updateRemoteConfig(newConfigValues);
    });

/**
 * Register a listener for the Firebase Remote Config
 *
 * This function is called when the Remote Config is changed
 */
exports.sendNewTimetableNotification = functions
    .region('europe-west1')
    .remoteConfig
    .onUpdate(async (versionMetadata) => {
        const config = admin.remoteConfig(); // Get Access to Firebase Remote Config

        // Get both the current and the previous template
        const newTemplate = config.getTemplate();
        const oldTemplate = config.getTemplateAtVersion(versionMetadata.versionNumber - 1);

        // Parse the templates
        const newValues = processRemoteConfigTemplate(await newTemplate);
        const oldValues = processRemoteConfigTemplate(await oldTemplate);

        let titlePrefix = "";
        let channel = CHANNEL_ID_DEFAULT; // By default, send the notification to everyone

        if (oldValues.middleSchool === newValues.middleSchool && oldValues.highSchool !== newValues.highSchool) {
            // Only the high school link has changed
            titlePrefix = "Lyzeum: ";
            channel = CHANNEL_ID_HIGH_SCHOOL;

        } else if (oldValues.middleSchool !== newValues.middleSchool && oldValues.highSchool === newValues.highSchool) {
            // Only the middle school link has changed
            titlePrefix = "Gymnasium: ";
            channel = CHANNEL_ID_MIDDLE_SCHOOL;

        } else if (oldValues.middleSchool === newValues.middleSchool && oldValues.highSchool === newValues.highSchool)
            return // None of the links have changed, do not send a notification

        // Get a random title and message for the notification
        const selectedTitle = TITLES[randomInt(0, TITLES.length - 1)];
        const selectedMessage = MESSAGES[randomInt(0, MESSAGES.length - 1)];

        const payload = {
            data: {
                title: titlePrefix + selectedTitle,
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
