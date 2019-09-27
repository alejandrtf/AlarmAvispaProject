const functions = require('firebase-functions');

const admin=require('firebase-admin');
admin.initializeApp();

var theme="NewSighting";
exports.notifySighting=functions.firestore.document('sightings/{sighting}')
		.onCreate((snap,context)=>{
			var newSighting=snap.data();
			var mBody='Localizado nuevo avistamiento de '+ newSighting.nestType;
			if(newSighting.nestStand !="" && newSighting.nestStand !=null)
				mBody+=' en un '+ newSighting.nestStand;
			if(newSighting.locality !="" && newSighting.locality !=null)
				mBody+=' en la localidad de '+newSighting.locality;
			
			const payload={
				notification:{
					title:'Nuevo avistamiento registrado!',
					body: mBody
				},
				data:{
					latitude:newSighting.latitude.toString(),
					longitude:newSighting.longitude.toString()
				}
			};
			
			return admin.messaging().sendToTopic(theme,payload)
				.then(function(response){
					console.log("Envío correcto",response);
				})
				.catch(function(error){
					console.log("Envío erróneo: ",error);
				});
		
		});

