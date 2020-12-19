	const socket = new WebSocket("wss://busch.click:3001");
	
	socket.onmessage = function(event) {
		console.debug("WebSocket message received:", event);
		const befehle = event.split(";");
		for each (a in befehle){
			const befehle2 = a.split(":")
			if(befehle2[0] == "[refreshPLayers]"){
				refreshPlayers(
			}	
		}	
	};

	let refreshPlayers = function(players){
		
		
	};	