import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import fetch from 'node-fetch';
import {JSDOM} from 'jsdom';
import ExplicitParameterValue = admin.remoteConfig.ExplicitParameterValue;

admin.initializeApp();

const SITE_URL = "https://brukenthal.ro";

const KEY_HIGH_SCHOOL = "url_high_school";
const KEY_MIDDLE_SCHOOL = "url_middle_school";

function updateRemoteConfig(middleSchoolUrl: string, highSchoolUrl: string): Promise<void> {
    console.log("High School Url: " + highSchoolUrl);
    console.log("Middle School Url: " + middleSchoolUrl);

    const config = admin.remoteConfig();
    return config.getTemplate()
        .then(template => {
            const parameters = template.parameters;
            const highSchoolDefault = parameters[KEY_HIGH_SCHOOL].defaultValue as ExplicitParameterValue;
            const middleSchoolDefault = parameters[KEY_MIDDLE_SCHOOL].defaultValue as ExplicitParameterValue;

            // Check if the values have changed
            if (highSchoolDefault.value !== highSchoolUrl || middleSchoolDefault.value !== middleSchoolUrl) {
                // Update the template
                parameters[KEY_HIGH_SCHOOL] = {
                    defaultValue: {value: highSchoolUrl}
                };
                parameters[KEY_MIDDLE_SCHOOL] = {
                    defaultValue: {value: middleSchoolUrl}
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
        })
}

exports.checkForNewTimetable = functions
    .region('europe-west1')
    .pubsub
    .schedule('every 30 minutes')
    .onRun(() => {
        fetch(SITE_URL).then(data => data.text())
            .then(data => {
                const dom = new JSDOM(data);
                const doc = dom.window.document;

                // Parse the HTML and get the urls
                const middleSchoolUrl =
                    doc.querySelector("li.menu-item-1320 a")!.getAttribute("href");
                const highSchoolUrl =
                    doc.querySelector("li.menu-item-1470 a")!.getAttribute("href");

                return updateRemoteConfig(SITE_URL + middleSchoolUrl, SITE_URL + highSchoolUrl);
            })
    });

exports.sendNewTimetableNotification = functions.region('europe-west1').remoteConfig.onUpdate(versionMetadata => {
    const messages = [
        "Yeah, I've got time",
        "Happy Spooktober",
        "Stay safe",
        "Hello there!",
        "Hippity hoppity your time is now my property 🔫",
        "How about I change your timetable?"
    ];
    const messageBody = messages[Math.floor(Math.random() * messages.length)];

    const condition = "\'all\' in topics";
    const payload = {
        data: {
            title: "Brukenthal Stundenplan",
            body: messageBody
        }
    };

    return admin.messaging().sendToCondition(condition, payload).then(_ =>
        console.log("Notification sent")
    ).catch(err => {
        console.error("Notification failed to send")
        console.error(err)
    });
});
