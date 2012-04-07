package org.wintrisstech.sensors;

import android.os.SystemClock;
import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

/**
 * An UltraSonicSensors instance is used to access three ultrasonic sensors
 * (left, front, and right) and read the measurements from these sensors.
 *
 * @author Erik Colban
 */
public class UltraSonicSensors {

    private static final String TAG = "UltraSonicSensor";
    private static final float CONVERSION_FACTOR = 308.8F;
//    private static final float TEMP_CELSIUS = 21.0F;
//    private static final float SPEED_OF_SOUND = 331.5F + (0.6F * TEMP_CELSIUS); // in m/s
    private static final int NUM_SAMPLES = 5;
    private static final int LEFT_ULTRASONIC_INPUT_PIN = 35;
    private static final int FRONT_ULTRASONIC_INPUT_PIN = 36;
    private static final int RIGHT_ULTRASONIC_INPUT_PIN = 37;
    private static final int STROBE_ULTRASONIC_OUTPUT_PIN = 15;//TODO  Needs to be changed back to 15 !!
    private final AnalogInput left;
    private final AnalogInput front;
    private final AnalogInput right;
    private DigitalOutput strobe;
    private int leftDistance;
    private int frontDistance;
    private int rightDistance;

    /**
     * Constructor of a UltraSonicSensors instance.
     *
     * @param ioio the IOIO instance used to communicate with the sensor
     * @throws ConnectionLostException  
     *
     */
    public UltraSonicSensors(IOIO ioio) throws ConnectionLostException {
        this.left = ioio.openAnalogInput(LEFT_ULTRASONIC_INPUT_PIN);
        this.front = ioio.openAnalogInput(FRONT_ULTRASONIC_INPUT_PIN);
        this.right = ioio.openAnalogInput(RIGHT_ULTRASONIC_INPUT_PIN);
        this.strobe = ioio.openDigitalOutput(STROBE_ULTRASONIC_OUTPUT_PIN);
    }

    /**
     * Makes a reading of the ultrasonic sensors and stores the results locally.
     * To access these readings, use {@link #getLeftDistance()},
     * {@link #getFrontDistance()}, and {@link #getRightDistance()}.
     *
     * @throws ConnectionLostException
     * @throws InterruptedException  
     */
    public void readUltrasonicSensors() throws ConnectionLostException, InterruptedException {
        float leftAccumulated = 0.0F;
        float frontAccumulated = 0.0F;
        float rightAccumulated = 0.0F;
        for (int n = 0; n < NUM_SAMPLES; n++) {
            long end = System.nanoTime() + 100000; // 100  us from now
            strobe.write(true);
            while (System.nanoTime() < end) {
            }
            strobe.write(false);
            SystemClock.sleep(100);
            leftAccumulated += left.getVoltage();
            frontAccumulated += front.getVoltage();
            rightAccumulated += right.getVoltage();
        }
        synchronized (this) {
            leftDistance = Math.round(leftAccumulated / NUM_SAMPLES * CONVERSION_FACTOR);
            frontDistance = Math.round(frontAccumulated / NUM_SAMPLES * CONVERSION_FACTOR);
            rightDistance = Math.round(rightAccumulated / NUM_SAMPLES * CONVERSION_FACTOR);
        }
    }
    
    /**
     * Test method to check the pulse emitted using an oscilloscope
     * 
     * @throws ConnectionLostException
     */
    public void testStrobe() throws ConnectionLostException {
        for (int n = 0; n < NUM_SAMPLES; n++) {
            long end = System.nanoTime() + 5000; // 5  us from now
            strobe.write(true);
            while (System.nanoTime() < end) {
            }
            strobe.write(false);
            SystemClock.sleep(100);
        }
    }

    /**
     * Gets the last read distance in cm of the left sensor 
     *
     * @return the left distance in cm
     */
    public synchronized int getLeftDistance() {
        return leftDistance;
    }

    /**
     * Gets the last read distance in cm of the front sensor
     *
     * @return the front distance in cm
     */
    public synchronized int getFrontDistance() {
        return frontDistance;
    }

    /**
     * Gets the last read distance in mm of the right sensor
     *
     * @return the right distance in mm
     */
    public synchronized int getRightDistance() {
        return rightDistance;
    }

    /**
     * Closes all the connections to the used pins
     */
    public void closeConnection() {
        left.close();
        front.close();
        right.close();
        strobe.close();
    }
}
