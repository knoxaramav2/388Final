const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp();

const db = admin.database();
const ref = db.ref();

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
 exports.helloWorld = functions.https.onRequest((request, response) => {
  response.send("Hello from Hoarders!");
 });

exports.getLocalResources = functions.https.onCall((data, context) => {
    return {
        data : data,
        context : context
    }
});

exports.testFunction = functions.https.onCall((data, response) => {
    return {
        value : "hello"
    }
});

exports.testFunction2 = functions.https.onRequest((request, response) => {
    response.data = JSON.stringify({
        hello : "hello",
        world : "world"
    });

    response.send(JSON.stringify({
        val : "abc",
        more : "def"
    }));
});

exports.test = (req, res) => {
    res.set('Access-Control-Allow-Methods', 'GET');
    res.set('Access-Control-Allow-Headers', 'Content-Type');
    res.set('Access-Control-Max-Age', '3600');
    res.status(204).send(req.data);
}

