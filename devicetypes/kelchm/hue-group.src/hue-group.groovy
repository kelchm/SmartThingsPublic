/**
 *  Hue Group
 *
 *  Original Author: SmartThings
 *  Modified By: Zachary Priddy and Matthew Kelch
 */
// for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Hue Group", namespace: "kelchm", author: "Matthew Kelch") {
		capability "Switch Level"
		capability "Actuator"
		capability "Color Control"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Test Capability" //Hope to replace with Transistion Time

		command "setAdjustedColor"
	}

	simulator {
		// TODO: define status and reply messages here
	}

		tiles (scale: 2){
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"setAdjustedColor"
			}
		}

		standardTile("reset", "device.reset", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"Reset Color", action:"reset", icon:"st.lights.philips.hue-single"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
	}

	main(["switch"])
	details(["switch", "levelSliderControl", "rgbSelector", "refresh", "reset"])
}

// parse events into attributes
def parse(description) {
	log.debug "parse() - $description"
	def results = []

	def map = description
	if (description instanceof String)  {
		log.debug "Hue Group stringToMap - ${map}"
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
	parent.groupOff(this, transitiontime)
	sendEvent(name: "switch", value: "off")
	sendEvent(name: "transitiontime", value: transitiontime)
}

def poll() {
	parent.poll()
}

def nextLevel() {
	def level = device.latestValue("level") as Integer ?: 0
	if (level < 100) {
		level = Math.min(25 * (Math.round(level / 25) + 1), 100) as Integer
	}
	else {
		level = 25
	}
	setLevel(level)
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
        parent.setGroupLevel(this, percent, transitiontime)
        sendEvent(name: "level", value: percent)
        sendEvent(name: "transitiontime", value: transitiontime)
    } else {
    	log.debug "Switch is off, storing requested ${percent}% as defaultLevel."
        state.defaultLevel = percent
    }
}

def setSaturation(percent) 
{
	def transitiontime = 4
	log.debug "Executing 'setSaturation'"
	parent.setGroupSaturation(this, percent, transitiontime)
	sendEvent(name: "saturation", value: percent)
	sendEvent(name: "transitiontime", value: transitiontime)
}

def setSaturation(percent, transitiontime) 
{
	log.debug "Executing 'setSaturation'"
	parent.setGroupSaturation(this, percent, transitiontime)
	sendEvent(name: "saturation", value: percent)
	sendEvent(name: "transitiontime", value: transitiontime)
}

def setHue(percent) 
{
	def transitiontime = 4
	log.debug "Executing 'setHue'"
	parent.setGroupHue(this, percent, transitiontime)
	sendEvent(name: "hue", value: percent)
	sendEvent(name: "transitiontime", value: transitiontime)
}

def setHue(percent, transitiontime) 
{
	log.debug "Executing 'setHue'"
	parent.setGroupHue(this, percent, transitiontime)
	sendEvent(name: "hue", value: percent)
	sendEvent(name: "transitiontime", value: transitiontime)
}

def setColor(value) {
	log.debug "setColor: ${value}"

	
	if(value.transitiontime)
	{
		sendEvent(name: "transitiontime", value: value.transitiontime)
	}
	else
	{
		sendEvent(name: "transitiontime", value: 4)
		value << [transitiontime: 4]
	}
	if (value.hex) 
	{
		sendEvent(name: "color", value: value.hex)
        
	} 
	else if (value.hue && value.saturation) 
	{
		def hex = colorUtil.hslToHex(value.hue, value.saturation)
		sendEvent(name: "color", value: hex)
	}
    if (value.hue && value.saturation) 
	{
		sendEvent(name: "saturation", value:  value.saturation)
        sendEvent(name: "hue", value:  value.hue)
	}
	if (value.level) 
	{
		sendEvent(name: "level", value: value.level)
	}
	if (value.switch) 
	{
		sendEvent(name: "switch", value: value.switch)
	}
	parent.setGroupColor(this, value)
}

def setAdjustedColor(value) {
	log.debug "setAdjustedColor: ${value}"
	def adjusted = value + [:]
	adjusted.hue = adjustOutgoingHue(value.hue)
	adjusted.level = null // needed because color picker always sends 100
	setColor(adjusted)
}

def save() {
	log.debug "Executing 'save'"
}

def refresh() {
	log.debug "Executing 'refresh'"
	parent.poll()
}

def adjustOutgoingHue(percent) {
	def adjusted = percent
	if (percent > 31) {
		if (percent < 63.0) {
			adjusted = percent + (7 * (percent -30 ) / 32)
		}
		else if (percent < 73.0) {
			adjusted = 69 + (5 * (percent - 62) / 10)
		}
		else {
			adjusted = percent + (2 * (100 - percent) / 28)
		}
	}
	log.info "percent: $percent, adjusted: $adjusted"
	adjusted
}




