	const socket = new WebSocket("wss://busch.click:3001");
	let clicked;
	
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
					getName(befehle2[1]);
					break;
				case "[setName]":
					setName(befehle2[1]);
					break;
				case "[setRole]":
					setRole(befehle2[1]);
					break;
				case "[setStatus]":
					setStatus(befehle2[1]);
					break;
				case "[setTimer]":
					setTimer(befehle2[1]);
					break;
				case "[displayText]":
					updateTextbox(befehle2[1]);
					break;
				case "[commenceGame]":
					forceSwitch();
					break;
				case "[updateCircle]":
					updateCircle(befehle2[1]);
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
	
	let setName = function(name) {
		let text = name;
		document.getElementById("ingameName").innerHTML = text;
	}
	
	let getName = function() {
		let text = document.getElementById("name").value;
		socket.send("[changeName]:" + text);
		setName(text);
	}
	
	let setRole = function(role) {
		let text = role;
		document.getElementById("ingameRole").innerHTML = text;
		console.log(document.getElementById("picture").innerHTML);
		document.getElementById("picture2").innerHTML = "<img src=\"recources/pictures/"+role+".png\" style=\"max-width: 100%\">";
		//console.log("<img src=\"recources/pictures/" + role +".png\" />");
	}
	
	let setStatus = function(curStatus) {
		let text = curStatus;
		document.getElementById("ingameStatus").innerHTML = text;
	}
	
	let setTimer = function(timer) {
		let text = timer;
		document.getElementById("ingameTimer").innerHTML = text;
	}
	
	let updateTextbox = function(txt) {
		let text = document.getElementById("textfield").innerHTML;
		console.log(txt);
		text =  "<p>" + txt + "</p>" + text ;
		console.log(text);
		document.getElementById("textfield").innerHTML = text;
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
	

	let test = function() {
		
	console.log(this);	
		
	}	

	let saveClicked = function() {
		
	}
	
	let getClicked = function() {
		
	}
	
	let updateCircle = function(message) {
		document.getElementById("circle").innerHTML = "";
		let message2 = message.split(",");
		message2.forEach(function(player){
			if(player != ""){
				let player2 = player.split("|");
				let text = document.getElementById("circle").innerHTML;
				console.log("<div class=\"playericon\" style=\"top:\"" + player2[2] + "cm\" left:\"" + player2[3] + "cm\"><img id=\"visibleImg\" name=\"" + player2[1] + "\" src=\"recources/pictures/dorfbewohner kreis.png\" onclick=\"test();\"/><p id=\"imageTxt\">" + player2[0] + "</p></div>");
				text = text + "<div class=\"playericon\" style=\"top:\"" + player2[2] + "cm\" left:\"" + player2[3] + "cm\"><img id=\"visibleImg\" name=\"" + player2[1] + "\" src=\"recources/pictures/dorfbewohner kreis.png\" onclick=\"test();\"/><p id=\"imageTxt\">" + player2[0] + "</p></div>";
				document.getElementById("circle").innerHTML = text;
			}	
		});	
	}	


	

