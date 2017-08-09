(function(){

	var wind = {

		mapObject: L.map('map', { zoomControl: false }).setView([31.628488, -99.921566], 7),
		pubnub: null,
		// this is where the linear ranges for expected wind speeds and power prices are set
		// used for scaling icons on map
		// somewhat arbitrary... chosen based on April 2017 max wind and max gusts throughout ERCOT
		windValueScale: [3, 30],
		// 5th and 95th percentile from ERCOT's July 2017 RTM prices (all nodes) - $/MWh
		priceValueScale: [15, 50],
		iconSizeScale: [8, 80],

		initialize: function() {

			L.tileLayer("https://api.mapbox.com/styles/v1/mapbox/dark-v9/tiles/256/{z}/{x}/{y}?access_token={accessToken}", {
    			attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
    			maxZoom: 18,
    			accessToken: 'pk.eyJ1IjoiaWZ3cmlnaHQiLCJhIjoiY2o0ZnJrbXdmMWJqcTMzcHNzdnV4bXd3cyJ9.1G8ErVmk7jP7PDuFp8KHpQ'
			}).addTo(wind.mapObject);

			var WindIcon = L.Icon.extend({
			    options: {iconUrl: 'img/wind.png'}
			});

			var DollarIcon = L.Icon.extend({
			    options: {iconUrl: 'img/dollar.png'}
			});

			wind.pubnub = new PubNub({
			    subscribeKey: "sub-c-97c0cd06-7a02-11e7-9c85-0619f8945a4f",
			    publishKey: "pub-c-9802bbd9-36b8-45db-a00b-c3a3d6bb0d2f",
			    ssl: true
			});

			var scale = function(value, valueScale) {
				// low extreme case
				if (value <= valueScale[0]) {
					return wind.iconSizeScale[0];
				// high extreme case
				} else if (value >= valueScale[1]) {
					return wind.iconSizeScale[0];
				// linear scale case
				} else {
					var perc = (value - valueScale[0]) / (valueScale[1] - valueScale[0]);
					var iconSize = Math.round((perc * (wind.iconSizeScale[1] - wind.iconSizeScale[0])) + wind.iconSizeScale[0]);
					console.log("icon size: ", iconSize);
					return iconSize
				};
			};

			wind.pubnub.addListener({
			    message: function(m) {
			        var msg = JSON.parse(m.message.replace(/\'/g, "\""));
			        console.log(msg);

			        if (msg.type == "price") {
			        	var iconDim = scale(msg.value, wind.priceValueScale);
			        	var iconAnchorDim = Math.round(iconDim / 2);
			        	var icon = new DollarIcon({iconSize: [iconDim, iconDim], iconAnchor: [iconAnchorDim, iconAnchorDim]});
			        	L.marker([msg.latitude, msg.longitude], {icon: icon})
			        		.addTo(wind.mapObject)
			        		.bindPopup(msg.value + " $/MWh", {'className' : 'custom'});

			        } else if (msg.type == "weather") {
			        	var iconDim = scale(msg.value, wind.windValueScale);
			        	var iconAnchorDim = Math.round(iconDim / 2);
			        	var icon = new WindIcon({iconSize: [iconDim, iconDim], iconAnchor: [iconAnchorDim, iconAnchorDim]});
			        	L.marker([msg.latitude, msg.longitude], {icon: icon})
			        		.addTo(wind.mapObject)
			        		.bindPopup(msg.value + " knots", {'className' : 'custom'});
			        };
			    }
			});

			wind.pubnub.subscribe({
		        channels: ['wind'] 
		    });
		}
	};
	this.wind = wind;
})();
