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
		
		forceSwitch();
		
		let dorfbewohner = document.getElementById("dorfbewohner").value;
		let hexe = document.getElementById("hexe").value;
		let amor = document.getElementById("amor").value;
		let seherin = document.getElementById("seherin").value;
		let leibwaechter = document.getElementById("leibwaechter").value;
		let werwolf = document.getElementById("werwolf").value;
		
		socket.send("[startGame]:" + "dorfbewohner_"+dorfbewohner+",hexe_"+hexe+",amor_"+amor+",seherin_"+seherin+",leibwaechter_"+leibwaechter+",werwolf_"+werwolf+";");
	}
	
	let forceSwitch = function() {
		$(document).ready(function(){
			$('#lobby').fadeOut();
		});
	}

	