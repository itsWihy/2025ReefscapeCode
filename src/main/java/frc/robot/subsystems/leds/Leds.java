package frc.robot.subsystems.leds;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj.util.Color8Bit;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.lib.util.CustomLEDPatterns;

import java.util.function.Supplier;

import static frc.lib.util.CustomLEDPatterns.*;
import static frc.robot.utilities.PortsConstants.LEDSTRIP_PORT_PWM;

public class Leds extends SubsystemBase {
    private static final AddressableLED ledstrip = new AddressableLED(LEDSTRIP_PORT_PWM);
    private static final AddressableLEDBuffer buffer = new AddressableLEDBuffer(LEDS_COUNT);

    public Leds() {
        ledstrip.setLength(LEDS_COUNT);
        ledstrip.setData(buffer);
        ledstrip.start();
    }

    public Command setLEDToPositionIndicator(Translation2d robotPosition, Translation2d targetPosition, double timeout) {
        return getCommandFromColours(() -> generatePositionIndicatorBuffer(
                new Color8Bit(Color.kRed),
                new Color8Bit(Color.kGreen),
                robotPosition,
                targetPosition
        ), timeout);
    }

    public Command setLEDStatus(LEDMode mode, double timeout) {
        return switch (mode) {
            case SHOOTER_LOADED -> getCommandFromColours(() -> generateFlashingBuffer(
                    new Color8Bit(Color.kOrange),
                    new Color8Bit(Color.kRed)
            ), timeout);

            case SHOOTER_EMPTY -> getCommandFromColours(() -> generateCirclingBuffer(
                    new Color8Bit(Color.kCyan),
                    new Color8Bit(Color.kWhite),
                    new Color8Bit(Color.kDeepPink)
            ), timeout);

            case DEBUG_MODE -> getCommandFromColours(() -> generateBreathingBuffer(
                    new Color8Bit(Color.kCyan),
                    new Color8Bit(Color.kWhite)
            ), timeout);

            case NOT_AT_AUTO_PLACE -> getCommandFromColours(() -> generateBreathingBuffer(
                    new Color8Bit(Color.kRed),
                    new Color8Bit(Color.kGreen)
            ), timeout);

            case BATTERY_LOW -> getCommandFromColours(() -> generateOutwardsPointsBuffer(new Color8Bit(Color.kRed)), timeout);

            default -> getCommandFromColours(CustomLEDPatterns::generateRainbowBuffer, 0);
        };
    }

    private Command getCommandFromColours(Supplier<Color8Bit[]> colours, double timeout) {
        if (timeout == 0)
            return Commands.run(() -> flashLEDStrip(colours.get()), this).ignoringDisable(true);

        return Commands.run(
                () -> flashLEDStrip(colours.get()), this).withTimeout(timeout).ignoringDisable(true);
    }

    private void flashLEDStrip(Color8Bit[] colours) {
        ledstrip.setData(getBufferFromColors(buffer, colours));
    }

    public enum LEDMode {
        SHOOTER_LOADED,
        SHOOTER_EMPTY,
        DEBUG_MODE,
        BATTERY_LOW,
        DEFAULT,
        NOT_AT_AUTO_PLACE
    }
}
