package com.keimgeraet.pi4keimgeraet.controller;

import com.pi4j.io.gpio.*;
import java.util.concurrent.TimeUnit;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by rener on 26.10.2016.
 */

@RestController
public class KeimgeraetController {

    private static GpioPinDigitalOutput pin1; //Wasser
    private static GpioPinDigitalOutput pin2; //Trommel
    private static GpioPinDigitalOutput pin3; //Luft

    @RequestMapping("/")
    public String greeting()
    {
        whereAmI("Ich bin in greeting()");
        return "Hello World!!!";
    }

    @RequestMapping("/light")
    public String light()
    {
        whereAmI("Ich bin in light()");
        if(pin1 == null){
            GpioController gpio = GpioFactory.getInstance();
            pin1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01,"Gpio_1", PinState.LOW);
            pin2 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02,"Gpio_2", PinState.LOW);
            pin3 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03,"Gpio_3", PinState.LOW);
        }
        pin1.toggle();
        pin2.toggle();
        pin3.toggle();

        return "Die LED wurde ein- oder asugeschalten!";
    }

    @RequestMapping("/initialize")
    public String Initialize()
    {
        whereAmI("Ich bin in initialize() a");
        if(pin1 == null){
            GpioController gpio = GpioFactory.getInstance();
            pin1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01,"Gpio_1", PinState.LOW);
            pin2 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02,"Gpio_2", PinState.LOW);
            pin3 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03,"Gpio_3", PinState.LOW);
            whereAmI("Ich bin in initialize() b");
        }

        return "Die Pins wurden initialisiert!";
    }


    Thread Phase1WasserThread = new Thread() {
        public void run() {
            whereAmI("Ich bin in Phase1WasserThread() a");
                //Wasser wird 1 Minute gegeben, für 59 Minuten gewartet und das ingesamt 24 mal wiederholt.
                for (int i = 24; i >= 1; i--) {
                    whereAmI("Ich bin in Phase1WasserThread() b");
                    pin1.high();
                    pin2.high();
                    try {
                        TimeUnit.MINUTES.sleep(1);
                        whereAmI("Ich bin in Phase1WasserThread() c");
                    } catch (InterruptedException e) {
                    }
                    pin1.low();
                    pin2.low();
                    try {
                        TimeUnit.MINUTES.sleep(59);
                    } catch (InterruptedException e) {
                    }

                }
            }
    };

    Thread Phase1TrommelThread = new Thread(){
        public void run() {
            //72 Mal da 24 Stunden * 60 Minuten = 1440 Minuten -
            //24 Stunden läuft es insgesamt
            whereAmI("Ich bin in Phase1TrommelThread() a");
            for (int s = 24; s >= 1; s--) {
                //4 Mal in der Stunde
                whereAmI("Ich bin in Phase1TrommelThread() b");
                for (int i = 4; i >= 1; i--) {
                    whereAmI("Ich bin in Phase1TrommelThread() c");
                    //45 Sekunden wird die Trommel gedreht
                    pin2.high();
                    whereAmI("Ich bin in Phase1TrommelThread() pin müsste leuchten");
                    try {
                        TimeUnit.SECONDS.sleep(45);
                    } catch (InterruptedException e) {
                    }
                    pin2.low();
                    whereAmI("Ich bin in Phase1TrommelThread() Pin müsste aus sein");
                    // 14 Minuten und 15 Sekunden wird gewartet danach das ganze 3 mal wiederholt
                    try {
                        TimeUnit.SECONDS.sleep(855);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    };

    Thread Phase2WasserThread = new Thread(){
        public void run() {
            //Wasser wird 1 Minute gegeben, für 479 (8 Stunden) Minuten gewartet und das ingesamt 3 mal wiederholt.
            for (int i = 3; i >= 1; i--) {
                pin1.high();
                pin2.high();
                Phase2LuftThread.run();
                try {
                    TimeUnit.MINUTES.sleep(1);
                } catch (InterruptedException e) {
                }

                pin1.low();
                pin2.low();
                try {
                    TimeUnit.MINUTES.sleep(479);
                } catch (InterruptedException e) {
                }
            }
        }
    };

    Thread Phase2LuftThread = new Thread(){
        public void run() {
            try {
                TimeUnit.MINUTES.sleep(60);
            } catch (InterruptedException e) {
            }
            pin3.high();
            try {
                TimeUnit.MINUTES.sleep(30);
            } catch (InterruptedException e) {
            }
            pin3.low();
        }
    };


    @RequestMapping("/phase1")
    public String Phase1()
    {
        whereAmI("Ich bin in Phase1() a");
        Phase1TrommelThread.start();
        whereAmI("Ich bin in Phase1() b");
        Phase1WasserThread.start();
        try{
            whereAmI("Ich bin in Phase1() c");
            Phase1TrommelThread.join();
            Phase1WasserThread.join();
        }
        catch(InterruptedException e){

        }
        return "Phase 1 fertig.";
    }

    @RequestMapping("/phase2")
    public String Phase2()
    {
        Phase2WasserThread.start();
        Phase1TrommelThread.start();
        Phase2LuftThread.start();

        try {
            Phase2WasserThread.join();
            Phase1TrommelThread.join();
            Phase2LuftThread.join();
        }
        catch(InterruptedException e){

        }
        return "Phase 2 fertig.";
    }
    @RequestMapping("/phase3")
    public String Phase3()
    {
        return "Phase 3 fertig.";
    }
    @RequestMapping("/start")
    public void start()
    {
        whereAmI("Ich bin in start() a");
        Initialize();
        whereAmI("Ich bin in start() b");
        Phase1();
        whereAmI("Ich bin in start() c");
        Phase2();
        Phase3();
    }

    public void whereAmI(String x)
    {
        System.out.println("Ich bin hier: " + x);
    }
}
