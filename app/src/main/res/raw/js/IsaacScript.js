(function(){
	var d = document,
	accordionToggles = d.querySelectorAll('.js-accordionTrigger'),
	setAria,
	setAccordionAria,
	switchAccordion,
  touchSupported = ('touchstart' in window),
  pointerSupported = ('pointerdown' in window);
  
  skipClickDelay = function(e){
    e.preventDefault();
    e.target.click();
  }

		setAriaAttr = function(el, ariaType, newProperty){
		el.setAttribute(ariaType, newProperty);
	};
	setAccordionAria = function(el1, el2, expanded){
		switch(expanded) {
      case "true":
      	setAriaAttr(el1, 'aria-expanded', 'true');
      	setAriaAttr(el2, 'aria-hidden', 'false');
      	break;
      case "false":
      	setAriaAttr(el1, 'aria-expanded', 'false');
      	setAriaAttr(el2, 'aria-hidden', 'true');
      	break;
      default:
				break;
		}
	};
//function
switchAccordion = function(e) {
  console.log("triggered");
	e.preventDefault();
	var thisAnswer = e.target.parentNode.nextElementSibling;
	var thisQuestion = e.target;
	if(thisAnswer.classList.contains('is-collapsed')) {
		setAccordionAria(thisQuestion, thisAnswer, 'true');
	} else {
		setAccordionAria(thisQuestion, thisAnswer, 'false');
	}
  	thisQuestion.classList.toggle('is-collapsed');
  	thisQuestion.classList.toggle('is-expanded');
		thisAnswer.classList.toggle('is-collapsed');
		thisAnswer.classList.toggle('is-expanded');
 	
  	thisAnswer.classList.toggle('animateIn');
	};
	for (var i=0,len=accordionToggles.length; i<len; i++) {
		if(touchSupported) {
      accordionToggles[i].addEventListener('touchstart', skipClickDelay, false);
    }
    if(pointerSupported){
      accordionToggles[i].addEventListener('pointerdown', skipClickDelay, false);
    }
    accordionToggles[i].addEventListener('click', switchAccordion, false);
  }
})();

function populateAccordion(){
	var accordion = document.getElementById("accordion_array");
//	var dl = document.getElementsByTagName("DL");
//	var array = readTextFile("/bus_stop_names");
	var array = [
			"1 Eddy/Hope/Benefit",
			"3 Warwick Ave",
			"6 Prairie / R.W. Zoo",
			"8 Jefferson Blvd",
			"9 Pascoag",
			"10 North Scituate Park & Ride",
			"11-1 Broad/North Main",
			"12 Arctic/Rte. 117 Express Park & Ride",
			"13 Coventry/Arctic/Warwick Mall",
			"14 West Bay",
			"17 Dyer / Pocasset",
			"18 Union Ave",
			"19 Plainfield / Westminster",
			"20 Elmwood Ave",
			"21 Reservoir /Garden City",
			"22 Pontiac Ave",
			"27 Broadway / Manton",
			"28 Broadway / Hartford",
			"29 Kent County",
			"30 Arlington / Oaklawn",
			"31 Cranston St",
			"32 East Providence/Wampanoag",
			"33 Riverside",
			"34 East Providence",
			"35 Rumford",
			"40 Butler/ Elmgrove/Tunnel",
			"49 Camp St / Miriam Hospital",
			"50 Douglas Ave",
			"51 Charles St",
			"52 Branch / Bryant Univ",
			"54 Lincoln / Woonsocket",
			"55 Admiral / Prov College",
			"56 Chalkstone Ave",
			"57 Smith Street",
			"58 Mineral Spring / N Providence",
			"59 North Smithfield/Lincoln Express Park & Ride",
			"60 Providence / Newport",
			"61-1 Tiverton/East Bay Park & Ride",
			"63 Broadway / Middletown Shop",
			"64 Newport / URI",
			"65 Wakefield Express Park & Ride",
			"66 URI / Galilee",
			"67 Bellevue / Mansions",
			"71 Broad St / Pawtucket Ave.",
			"72 Weeden / Central Falls",
			"73 Fairlawn / CCRI",
			"75 Dexter / Lincoln Mall",
			"76 Central Ave",
			"78 Beverage Hill / Newport Ave",
			"80 Armistice Blvd"
	]
	
//	 <a href="#accordion2" aria-expanded="false" aria-controls="accordion2" class="accordion-title accordionTitle js-accordionTrigger">	
	for(var i = 0; i < array.length; i++){
		
		var dt = document.createElement('dt');
		var a = document.createElement('a');
		
		var href = document.createAttribute("href");
		href.value = "#accordion1";
		
		var aria_expanded = document.createAttribute("aria-expanded");
		aria_expanded.value = "false";
		
		var aria_controls = document.createAttribute("aria-controls");
		aria_controls.value = "accordion_isaac" + i;
		
		var class_att = document.createAttribute("class");
		class_att.value = "accordion-title accordionTitle js-accordionTrigger";
		
		a.setAttributeNode(href);
		a.setAttributeNode(aria_controls);
		a.setAttributeNode(aria_controls);
		a.setAttributeNode(class_att);
		a.innerHTML = array[i];
		
		dt.appendChild(a);
		var ddElement = createDTElement(i, array)

		accordion.appendChild(dt);
		accordion.appendChild(ddElement);
	}
};

function createDTElement(i, array){
	var dd = document.createElement('dd');
	var div = document.createElement('div');
	
	div.setAttribute("class", "acc-cont");
	/*
	<dd class="accordion-xcontent accordionItem is-collapsed" id="accordion1" aria-hidden="true">
	<div class="acc-cont">
	    <a class="route-label" href="http://www.google.com">T.F. Green</a>
	    <span class="route-label">Warwick Ave. at Shaws</span>*/
	
	dd.setAttribute("class", "accordion-xcontent accordionItem is-collapsed");
	dd.setAttribute("id", "accordion1");
	dd.setAttribute("aria-hidden", "true");

	var innerArray = [
	                  "Stop 1",
	                  "Stop 2",
	                  "Stop 3",
	                  "Stop 4",
	                  "Stop 5",
	                  "Stop 6",
	                  "Stop 7",
	                  "Stop 8",
	                  "Stop 9",
	                  "Stop 10",
	                  "Stop 11",
	                  "Stop 12",
	                  "Stop 13",
	                  "Stop 14",
	                  ];
	for(var j = 0; j < innerArray.length; j++){
		var a = document.createElement('a');
		var button = document.createElement('button');
		a.setAttribute("class", "route-label");
		a.setAttribute("href", "http://www.google.com");
		a.setAttribute("id", "stop" + i);
		a.innerHTML = innerArray[j];
		button.setAttribute("type", "button");
		button.innerHTML = "Click for: " + innerArray[i];
		div.appendChild(button);
	}
	
	dd.appendChild(div);

	return dd;
	
};

function readTextFile(file)
{
	var fs = require("fs");
	var text = fs.readFileSync(file);
	var textByLine = text.split("\n");
	var array = data.split(',');
	return array;
};
