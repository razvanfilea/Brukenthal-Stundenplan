import * as functions from 'firebase-functions';
import * as http from 'http';
import * as admin from 'firebase-admin'

admin.initializeApp();

exports.checkForNewTimetable = functions
    .region('europe-west1')
    .pubsub
    .schedule('every 23 hours')
    .onRun((context) => {
        const options = {
            host: 'https://brukenthal.ro',
            port: 80,
            path: '/index.html'
        };

        const req = http.get(options, (res) => {
            let content = "";

            res.setEncoding("utf8");
            res.on("data", (chunk) => {
                content += chunk;
            });

            res.on("end", () => {
                functions.logger.log(content);
            });

            req.end();
        }).on('error', (e) => {
            functions.logger.error("Error: " + e);
        });

        return req;
    });

exports.sendNewTimetableNotification = functions.region('europe-west1').remoteConfig.onUpdate(versionMetadata => {
    /*const config = admin.remoteConfig();
    return config.getTemplate()
        .then(function (template) {
            console.log('ETag from server: ' + template.etag);
            console.log(JSON.stringify(template));
            JSON.parse(template.parameters.)

        })
        .catch(function (err) {
            console.error('Unable to get template');
            console.error(err);
        });*/

    const condition = "\'all\' in topics";
    const payload = {
        data: {
            title: 'Hey! You there!',
            body: 'I heard that there\'s a new Stundenplan in town'
        }
    };

    return admin.messaging().sendToCondition(condition, payload).then(_ =>
        console.log("Notification sent!")
    ).catch(_ => {
        console.log("Notification failed to send")
    });
});
