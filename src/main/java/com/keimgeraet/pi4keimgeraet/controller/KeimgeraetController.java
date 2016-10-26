package com.keimgeraet.pi4keimgeraet.controller;

import com.pi4j.io.gpio.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by rener on 26.10.2016.
 */

@RestController
public class KeimgeraetController {

    private static GpioPinDigitalOutput pin;
    @RequestMapping("/")
    public String greeting()
    {
        return "Hello World";
    }

    @RequestMapping("/light")
    public String light()
    {

        if(pin == null){
            GpioController gpio = GpioFactory.getInstance();
            pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01,"MyLED", PinState.LOW);
        }
        pin.toggle();

        return "Die LED wurde ein- oder asugeschalten!";
    }
}
