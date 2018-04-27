const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendNotification2 = functions.https.onCall( (data, context) => {
	console.log('test', 'sendNotification!1');
	const receiveToken = data.receiverToken;
	console.log('test', 'sendNotification!2');
	var payload = {
		data : {
			dataType: data.dataType,
			requestType: data.requestType,
			message: data.message,
			senderToken: data.senderToken,
			senderName: data.senderName,
			senderUid: data.senderUid,
			receiverToken: data.receiverToken
		}
	};
	
	console.log('test', 'sendNotification!3');
	
	return admin.messaging().sendToDevice(receiveToken, payload).then(function(response) {
		console.log("Successfully sent message:", response);
		return {
			result : true
		}
	}).catch(function(error) 
	{
		console.log("Error sending message:", error);
		return {
			result : false
		}
	});
});