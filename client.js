	const socket = new WebSocket("wss://busch.click:3001");
	
	socket.onmessage = function(event) {
		console.debug("WebSocket message received:", event);
		//const input = "" + event;
		let befehle = event.data.split(";");
		console.log(befehle);
		befehle.forEach(function(a){ 
			let befehle2 = a.split(":");
			console.log(befehle2);
			console.log(befehle2.length);
			if(befehle2[0] == "[refreshPlayers]"){
				//befehle2 = ;
				//let befehle3 = befehle2[0];
				refreshPlayers(befehle2[1].split(","));
			}	
		});	
	};

	let refreshPlayers = function(players){
		let text = "";
		players.forEach(function(player){
			if(player != ""){
			text = text + "<p>" + player + "</p>";	
			}
		});	
		console.log(text);
		document.getElementById("playerspan").innerHTML = text;
		
	};	