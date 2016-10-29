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
    private static GpioPinDigitalOutput pin4; //Wasserstand

    //Variablen für Phase1
    int p1wDauer = 30;           //Die Dauer, für das das Wasser engeschalten wird. (In Sekunden)
    int p1wAlleXSekunden = 3600 - p1wDauer;   //Alle x Minuten wird das Wasser eingeschalten. (In Sekunden)
    int p1wGesDauer = 1440;       //Die Gesamtdauer, wie lange diese Vorgänge stattfinden sollen. (In Minuten)
    int p1wZyklen = (int)(((p1wGesDauer * 60) / (p1wAlleXSekunden + p1wDauer))); //Anzahl der Zyklen bei denen das Wasser eingeschaltet wird und diese in die Gesamtdauer passt.

    int p1tDauer = 45;          //Dauer, die sich die Trommel dreht (In Sekunden)
    int p1tAlleXSekunden = 900 - p1tDauer; //Alle X Sekunden läuft die Trommel (Dauer der Trommeldrehung abgezogen) (in Sekunden)
    int p1tGesDauer = 1440;     //In Minuten
    int p1tZyklen = (int)((p1tGesDauer * 60) / p1tAlleXSekunden);   //Automatische berechnung der Zyklen

    //Variablen für Phase1
    int p2wDauer = 60;           //Die Dauer, für das das Wasser engeschalten wird. (In Sekunden)
    int p2wAlleXSekunden = 28800 - p2wDauer;   //Alle x Minuten wird das Wasser eingeschalten. (In Sekunden)
    int p2wGesDauer = 4320;       //Die Gesamtdauer, wie lange diese Vorgänge stattfinden sollen. (In Minuten)
    int p2wZyklen = (int)(((p2wGesDauer * 60) / (p2wAlleXSekunden + p1wDauer))); //Anzahl der Zyklen bei denen das Wasser eingeschaltet wird und diese in die Gesamtdauer passt.

    int p2tDauer = 45;          //Dauer, die sich die Trommel dreht (In Sekunden)
    int p2tAlleXSekunden = 900 - p1tDauer; //Alle X Sekunden läuft die Trommel (Dauer der Trommeldrehung abgezogen) (in Sekunden)
    int p2tGesDauer = 4320;     //In Minuten
    int p2tZyklen = (int)((p2tGesDauer * 60) / p2tAlleXSekunden);   //Automatische berechnung der Zyklen

    int p2lDauer = 1800; //Dauer, wie lange Luft reingelassen wird.
    int p2lAlleXSekunden = 3600; //Alle X Sekunden wird luft reingelassen
    int p2lGesDauer = 4320; //Wie lange es insgesamt läuft (in Minuten)
    int p2lZyklus = (int)((p2lGesDauer * 60) / (p2lAlleXSekunden + p2lDauer)); //Wartedauer UND LuftAnDauer

    @RequestMapping("/")
    public String greeting()
    {
        System.out.println("Die Applikation wurde gestartet! Sie befinden sich nun in '/' (root).");
        return "Hello World!!!";
    }

    @RequestMapping("/light")
    public String light()
    {
        if(pin1 == null){
            GpioController gpio = GpioFactory.getInstance();
            pin1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01,"Gpio_1", PinState.LOW);
            pin2 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02,"Gpio_2", PinState.LOW);
            pin3 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03,"Gpio_3", PinState.LOW);
            pin4 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "Gpio 4", PinState.getState(true));
        }

        if(pin1.isLow() || pin2.isLow() || pin3.isLow())
        {
            pin1.high();
            pin2.high();
            pin3.high();
            System.out.println("Gpio 1, 2, 3 sind nun an!");
        }
        else if(pin1.isHigh() || pin2.isHigh() || pin3.isHigh())
        {
            pin1.low();
            pin2.low();
            pin3.low();
            System.out.println("Gpio 1, 2, 3 sind nun aus!");
        }

        return "Die LED wurde ein- oder asugeschalten!";
    }

    @RequestMapping("/initialize")
    public String Initialize()
    {
        if(pin1 == null){
            GpioController gpio = GpioFactory.getInstance();
            pin1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01,"Gpio_1", PinState.LOW);
            pin2 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02,"Gpio_2", PinState.LOW);
            pin3 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03,"Gpio_3", PinState.LOW);
            pin4 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "Gpio 4", PinState.getState(true));
            System.out.println("Gpio 1, 2, 3, 4 wurden initialisiert und sind nun bereit benutzt zu werden");
        }

        return "Die Pins wurden initialisiert!";
    }


    Thread Phase1WasserThread = new Thread() {
        public void run() {
            //Es werden p1wZyklen Zyklen durchlaufen mit  p1wDauer bei der das Wasser läuft | Wasser & Trommel läuft!
            for (int i = p1wZyklen; i >= 1; i--) {
                while (pin4.isHigh()) {
                    try {
                        pin1.high();
                        pin2.high();
                        try {
                            System.out.println("Das Wasser wurde für " + p1wDauer + " Sekunden angeschalten!");
                            TimeUnit.SECONDS.sleep(p1wDauer);
                        } catch (InterruptedException e) {
                        }
                        pin1.low();
                        pin2.low();
                        try {
                            System.out.println("Das Wasser wurde für " + p1wAlleXSekunden + "( ca. " + p1wAlleXSekunden / 60 + " Minuten )" + " Sekunden ausgeschalten!");
                            TimeUnit.SECONDS.sleep(p1wAlleXSekunden);
                        } catch (InterruptedException e) {
                        }
                    } catch (InternalError e) {
                        System.out.println("Wasserstand ist zu niedrig! " + e);
                    }
                }

            }
        }
    };

    Thread Phase1TrommelThread = new Thread(){
        public void run() {
                for (int i = p1tZyklen; i >= 1; i--) {
                    pin2.high();
                    try {
                        System.out.println("Die Trommel ist für " + p1tDauer + " Sekunden angeschalten");
                        TimeUnit.SECONDS.sleep(p1tDauer);
                    } catch (InterruptedException e) {
                    }
                    pin2.low();
                    try {
                        System.out.println("Die Trommel ist für " + p1tAlleXSekunden + "( ca. " + p1tAlleXSekunden/60 + " Minuten )" + " Sekunden ausgeschalten");
                        TimeUnit.SECONDS.sleep(p1tAlleXSekunden);
                    } catch (InterruptedException e) {
                    }
                }
            }
    };

    Thread Phase2WasserThread = new Thread(){
        public void run() {
            for (int i = p2wZyklen; i >= 1; i--) {
                while(pin4.isHigh()) {
                    try {
                        pin1.high();
                        pin2.high();
                        try {
                            System.out.println("Das Wasser ist für " + p2wDauer + " Sekunden angeschalten");
                            TimeUnit.SECONDS.sleep(p2wDauer);
                        } catch (InterruptedException e) {
                        }

                        pin1.low();
                        pin2.low();
                        try {
                            System.out.println("Das Wasser ist für " + p2wAlleXSekunden + "( ca. " + p2wAlleXSekunden / 60 + " Minuten )" + " Sekunden ausgeschalten");
                            TimeUnit.SECONDS.sleep(p2wAlleXSekunden);
                        } catch (InterruptedException e) {
                        }
                    } catch (InternalError e){
                        System.out.println("Der Wasserstand ist zu niedrig! " + e);
                    }
                }
            }
        }
    };

    Thread Phase2TrommelThread = new Thread(){
        public void run() {
            for (int i = p2tZyklen; i >= 1; i--) {
                pin2.high();
                try {
                    System.out.println("Die Trommel ist für " + p2tDauer + " Sekunden angeschalten");
                    TimeUnit.SECONDS.sleep(p2tDauer);
                } catch (InterruptedException e) {
                }
                pin2.low();
                try {
                    System.out.println("Die Trommel ist für " + p2tAlleXSekunden + "( ca. " + p2tAlleXSekunden/60 + " Minuten )" + " Sekunden ausgeschalten");
                    TimeUnit.SECONDS.sleep(p2tAlleXSekunden);
                } catch (InterruptedException e) {
                }
            }
        }
    };

    Thread Phase2LuftThread = new Thread(){
        public void run() {
            for(int i = p2lZyklus; i >= 1; i--) {
                pin3.low();
                try {
                    System.out.println("Die Luft ist für " + p2lAlleXSekunden + "( ca. " + p2lAlleXSekunden/60 + " Minuten )" + " Sekunden ausgeschalten");
                    TimeUnit.SECONDS.sleep(p2lAlleXSekunden);
                } catch (InterruptedException e) {
                }
                pin3.high();
                try {
                    System.out.println("Die Luft ist für " + p2lDauer + "( ca. " + p2lDauer/60 + " Minuten )" + " Sekunden ausgeschalten");
                    TimeUnit.SECONDS.sleep(p2lDauer);
                } catch (InterruptedException e) {
                }
                pin3.low();
            }
        }
    };


    @RequestMapping("/phase1")
    public String Phase1()
    {
        System.out.println("Phase 1 läuft aktuell...");
        Phase1TrommelThread.start();
        Phase1WasserThread.start();
        try{
            System.out.println("Phase 1 wird abgeschlossen!");
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
        System.out.println("Phase 1 läuft aktuell...");
        Phase2WasserThread.start();
        Phase2TrommelThread.start();
        Phase2LuftThread.start();

        try {
            System.out.println("Phase 1 wird abgeschlossen!");
            Phase2WasserThread.join();
            Phase2TrommelThread.join();
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
        Initialize();
        Phase1();
        Phase2();
        Phase3();
    }

    public void whereAmI(String x)
    {
        System.out.println("Ich bin hier: " + x);
    }
}
