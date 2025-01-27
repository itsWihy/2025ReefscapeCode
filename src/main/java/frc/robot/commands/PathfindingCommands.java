package frc.robot.commands;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.DeferredCommand;
import frc.lib.util.flippable.Flippable;
import frc.robot.subsystems.swerve.SwerveCommands;

import java.util.Set;

import static frc.lib.util.flippable.FlippableUtils.flipAboutXAxis;
import static frc.lib.util.flippable.FlippableUtils.flipAboutYAxis;
import static frc.robot.RobotContainer.POSE_ESTIMATOR;
import static frc.robot.RobotContainer.SWERVE;
import static frc.robot.utilities.FieldConstants.*;
import static frc.robot.utilities.FieldConstants.ReefFace.*;

public class PathfindingCommands {
    private static final double PID_PATHFIND_THRESHOLD_REEF = 0.8;
    private static final double PID_PATHFIND_THRESHOLD_FEEDER = 0.8;

    public static DeferredCommand pathfindToLeftBranch() {
        return new DeferredCommand(() -> {
            final Pose2d targetPose = decideReefFace().getLeftBranch();

            return isRobotInProximity(targetPose, PID_PATHFIND_THRESHOLD_REEF) ?
                    SwerveCommands.goToPosePID(targetPose) :
                    SwerveCommands.goToPoseBezier(targetPose);
        }, Set.of(SWERVE));
    }

    public static DeferredCommand pathfindToRightBranch() {
        return new DeferredCommand(() -> {
            final Pose2d targetPose = decideReefFace().getRightBranch();

            return isRobotInProximity(targetPose, PID_PATHFIND_THRESHOLD_REEF) ?
                    SwerveCommands.goToPosePID(targetPose) :
                    SwerveCommands.goToPoseBezier(targetPose);
        }, Set.of(SWERVE));
    }

    public static DeferredCommand pathfindToFeeder() {
        return new DeferredCommand(() -> {
            final Pose2d targetPose = decideFeederPose();

            return isRobotInProximity(targetPose, PID_PATHFIND_THRESHOLD_FEEDER) ?
                    SwerveCommands.goToPosePID(targetPose) :
                    SwerveCommands.goToPoseBezier(targetPose);
        }, Set.of(SWERVE));
    }

    private static ReefFace decideReefFace() {
        final Translation2d robotPose = POSE_ESTIMATOR.getCurrentPose().getTranslation();
        final Translation2d distanceToReef = REEF_CENTER.get().minus(robotPose);

        final double angle = Math.toDegrees(Math.atan2(distanceToReef.getY(), distanceToReef.getX()));

        if (150 <= angle || angle < -150) return Flippable.isRedAlliance() ? FACE_0 : FACE_3;
        if (-30 <= angle && angle < 30) return Flippable.isRedAlliance() ? FACE_3 : FACE_0;
        if (90 <= angle) return Flippable.isRedAlliance() ? FACE_1 : FACE_4;
        if (-90 <= angle && angle < -30) return Flippable.isRedAlliance() ? FACE_4 : FACE_1;
        if (-150 <= angle && angle < -90) return Flippable.isRedAlliance() ? FACE_5 : FACE_2;

        return Flippable.isRedAlliance() ? FACE_2 : FACE_5;
    }

    private static Pose2d decideFeederPose() {
        Pose2d originalPose = BLUE_BOTTOM_FEEDER_INTAKE_POSE;

        if (POSE_ESTIMATOR.getCurrentPose().getY() - FIELD_WIDTH / 2 > 0)
            originalPose = flipAboutXAxis(originalPose);

        if (Flippable.isRedAlliance())
            originalPose = flipAboutYAxis(originalPose);

        return originalPose;
    }

    private static boolean isRobotInProximity(Pose2d pose2d, double thresholdMetres) {
        return POSE_ESTIMATOR.getCurrentPose().getTranslation().getDistance(pose2d.getTranslation()) < thresholdMetres;
    }
}