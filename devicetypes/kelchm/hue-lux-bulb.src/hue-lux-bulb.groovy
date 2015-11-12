/**
 *  Hue Lux Bulb
 *
 *  Original Author: SmartThings
 *  Modified By: Zachary Priddy and Matthew Kelch
 */
// for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Hue Lux Bulb", namespace: "kelchm", author: "Matthew Kelch") {
		capability "Switch Level"
		capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"

		capability "Test Capability" //Hope to replace with Transistion Time
	}

	simulator {
		// TODO: define status and reply messages here
	}

    tiles(scale: 2) {
        multiAttributeTile(name:"rich-control", type: "lighting", canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
            }
            tileAttribute ("device.level", key: "SECONDARY_CONTROL") {
	            attributeState "level", label: 'Level ${currentValue}%'
			}
        }
    
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
			state "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			state "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
			state "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
        }
           
        standardTile("refresh", "device.switch", inactiveLabel: false, height: 2, width: 2, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }        

        main(["switch"])
        details(["rich-control", "refresh"])
    } 

}

// parse events into attributes
def parse(description) {
	log.debug "parse() - $description"
	def results = []

	def map = description
	if (description instanceof String)  {
		log.debug "Hue Bulb stringToMap - ${map}"
		map = stringToMap(description)
	}

	if (map?.name && map?.value) {
		results << createEvent(name: "${map?.name}", value: "${map?.value}")
	}

	results
}

// handle commands
def on() 
{
	on(1)
}

def on(transitiontime)
{
    if(state.defaultLevel > 0) {
    	setLevel(state.defaultLevel, 1, true)
        state.defaultLevel = 0
    } else {
    	setLevel(device.currentValue('level'), 1, true)
    }
    sendEvent(name: "switch", value: "on")
    sendEvent(name: "transitiontime", value: transitiontime)
}

def off() 
{
    off(1)
}

def off(transitiontime)
{
	parent.off(this, transitiontime)
	sendEvent(name: "switch", value: "off")
	sendEvent(name: "transitiontime", value: transitiontime)
}

def poll() {
	parent.poll()
}

def setLevel(percent) 
{
	setLevel(percent, 1)
}

def setLevel(percent, transitiontime) 
{
	setLevel(percent, transitiontime, false)
}

def setLevel(percent, transitiontime, switchOn)
{
	if(percent == 0)
    {
        log.debug "Enforcing min setLevel of 1%"
        percent = 1
    }
 
    if(device.currentValue('switch') == "on" || switchOn)
    {
        log.debug "setLevel: ${percent}% ${transitiontime}s"
        parent.setLevel(this, percent, transitiontime)
        sendEvent(name: "level", value: percent)
        sendEvent(name: "transitiontime", value: transitiontime)
    } else {
    	log.debug "Switch is off, storing requested ${percent}% as defaultLevel."
        state.defaultLevel = percent
    }
}

def save() {
	log.debug "Executing 'save'"
}

def refresh() {
	log.debug "Executing 'refresh'"
	parent.poll()
}
