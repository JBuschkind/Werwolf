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
			
			switch(befehle2[0]) {
				case "[refreshPlayers]":
					refreshPlayers(befehle2[1].split(","));
					break;
				case "[getName]":
					getName(befehle[1]);
					break;
				case "[setName]":
					setName(befehle[1]);
					break;
				case "[setRole]":
					setRole(befehle[1]);
					break;
				case "[setStatus]":
					setStatus(befehle[1]);
					break;
				case "[setTimer]":
					setTimer(befehle[1]);
					break;
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
	
	document.getElementById("name").onchange = function() {
		let text = document.getElementById("name");
		socket.send("[changeName]:" + text);
	}
	
	let setName = function(name) {
		let text = name;
		document.getElementById("ingameName").innerHTML = text;
	}
	
	let setRole = function(role) {
		let text = role;
		document.getElementById("ingameRole").innerHTML = text;
	}
	
	let setStatus = function(curStatus) {
		let text = curStatus;
		document.getElementById("ingameStatus").innerHTML = text;
	}
	
	let setTimer = function(timer) {
		let text = timer;
		document.getElementById("ingameTimer").innerHTML = text;
	}
	
	let switchview = function() {
		console.log("Hallo");
	}

	
	let switchview = function() {
		console.log("Hallo");
	}