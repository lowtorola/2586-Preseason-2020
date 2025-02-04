/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import java.io.IOException;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Constants.DriveConstants;
import frc.robot.Constants.LimelightConstants;
import frc.robot.Constants.OIConstants;
import frc.robot.Constants.ShooterConstants;
import frc.robot.commands.AutoShoot;
import frc.robot.commands.DefaultDrive;
import frc.robot.commands.DriveStraight;
import frc.robot.commands.FollowPathCommand;
import frc.robot.commands.HoodLogic;
import frc.robot.commands.ShooterPreload;
import frc.robot.commands.ShooterSpool;
import frc.robot.lib.limelight;
import frc.robot.commands.LimelightTarget;
import frc.robot.subsystems.DriveSubsystem;
import frc.robot.subsystems.IndexerSubsystem;
import frc.robot.subsystems.IntakeSubsystem;
import frc.robot.subsystems.LEDSubsystem;
import frc.robot.subsystems.LimelightSubsystem;
import frc.robot.subsystems.ShooterSubsystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.PIDCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;

/**
 * This class is where the bulk of the robot should be declared. Since
 * Command-based is a "declarative" paradigm, very little robot logic should
 * actually be handled in the {@link Robot} periodic methods (other than the
 * scheduler calls). Instead, the structure of the robot (including subsystems,
 * commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  // The robot's subsystems and commands are defined here...
  private final DriveSubsystem robotDrive = new DriveSubsystem();
  private final ShooterSubsystem shooterControl = new ShooterSubsystem();
  private final IndexerSubsystem indexerControl = new IndexerSubsystem();
  private final IntakeSubsystem intakeControl = new IntakeSubsystem();
  private final LimelightSubsystem limelight = new LimelightSubsystem();
  private final LEDSubsystem LEDsub = new LEDSubsystem();

  Joystick drive_Stick = new Joystick(OIConstants.kDriveControllerPort);
 // Joystick operator_Stick = new Joystick(OIConstants.kOperatorControllerPort);

  private SendableChooser<Command> m_autonomousChooser = new SendableChooser<>();

  private final String slalomPath = "paths/SlalomPath.wpilib.json";
  private final String barrelRacingPath = "paths/BarrelRacingPath.wpilib.json";
  private final String[] bouncePath = { "paths/BouncePath1.wpilib.json", "paths/BouncePath2.wpilib.json",
      "paths/BouncePath3.wpilib.json", "paths/BouncePath4.wpilib.json" };

  /**
   * The container for the robot. Contains subsystems, OI devices, and commands.
   * 
   * @throws IOException
   */
  public RobotContainer() throws IOException {
    // Configure the button bindings
    configureButtonBindings();
    robotDrive.setDefaultCommand(
      // A split-stick arcade command, with forward/backward controlled by the left
      // hand, and turning controlled by the right.
      new DefaultDrive(
          robotDrive,
          () -> -drive_Stick.getRawAxis(1),
          () -> drive_Stick.getRawAxis(2)));
    //SmartDashboard.put(shooterControl);

    SmartDashboard.putData(m_autonomousChooser);
    m_autonomousChooser.setDefaultOption("Slalom Path", new FollowPathCommand(robotDrive, slalomPath));
    m_autonomousChooser.addOption("Barrel Racing", new FollowPathCommand(robotDrive, barrelRacingPath));
    m_autonomousChooser.addOption("Bounce Path", new SequentialCommandGroup(
      new FollowPathCommand(robotDrive, bouncePath[0]),
      new FollowPathCommand(robotDrive, bouncePath[1]),
      new FollowPathCommand(robotDrive, bouncePath[2]),
      new FollowPathCommand(robotDrive, bouncePath[3])
    ));


    LEDsub.LEDset();

  }

  /**
   * Use this method to define your button->command mappings.  Buttons can be created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a
   * {@link edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {

    new JoystickButton(drive_Stick, OIConstants.kShooterOnButton) // O
      .whenHeld(new ParallelCommandGroup(new InstantCommand(shooterControl::runShooter))) // new ShooterSpool(shooterControl, limelight COMMA
       // new HoodLogic(limelight, shooterControl)))
      .whenReleased(new InstantCommand(shooterControl::stopShooter, shooterControl));
/*
    new JoystickButton(drive_Stick, OIConstants.kShooterOnButton) // O
      .whenHeld(new ShooterSequence(shooterControl, limelight))
      .whenReleased(new InstantCommand(shooterControl::stopShooter));
*/
    new JoystickButton(drive_Stick, OIConstants.kFeederOnButton) // left bumper
    .whenPressed(new InstantCommand(shooterControl::runFeed, shooterControl))
    .whenReleased(new InstantCommand(shooterControl::stopFeed, shooterControl))
    .whenPressed(new InstantCommand(indexerControl::runIndexer))
    .whenReleased(new InstantCommand(indexerControl::stopIndexer));

    new JoystickButton(drive_Stick, OIConstants.kIntakeOnButton) // left trigger button
    .whenPressed(new InstantCommand(intakeControl::startIntake))
    .whenReleased(new InstantCommand(intakeControl::stopIntake));

    new JoystickButton(drive_Stick, OIConstants.kIntakeDeployButton) // share
    .whenPressed(new InstantCommand(intakeControl::deployIntake));

    new JoystickButton(drive_Stick, OIConstants.kIntakeRetractButton) // options
    .whenPressed(new InstantCommand(intakeControl::retractIntake));
 
    new JoystickButton(drive_Stick, OIConstants.kFeederPreloadButton) // central pad
    .whenPressed(new ShooterPreload(shooterControl), true)
    .whenReleased(new InstantCommand(shooterControl::stopFeed, shooterControl));

    new JoystickButton(drive_Stick, OIConstants.kLimelightAimButton)  // right bumper
    .whenHeld(new LimelightTarget(LimelightConstants.kTargetAngle, robotDrive, limelight));

    new JoystickButton(drive_Stick, OIConstants.kXButton)
    .whenHeld(new AutoShoot(shooterControl, robotDrive, limelight, indexerControl, intakeControl))
    .whenReleased(new ParallelCommandGroup(
      new InstantCommand(shooterControl::stopShooter, shooterControl),
      new InstantCommand(indexerControl::stopIndexer, indexerControl),
      new InstantCommand(intakeControl::stopIntake, intakeControl),
      new InstantCommand(shooterControl::stopFeed)
    ));

/*
    new JoystickButton(drive_Stick, OIConstants.kShooterAutoButton) // X
    .whenHeld(new ParallelCommandGroup(
      new PIDCommand( 
        new PIDController(ShooterConstants.kP, ShooterConstants.kI, ShooterConstants.kD),
        // Close the loop on Shooter Current RPM
        shooterControl::getMeasurement,
        // get setpoint
        ShooterConstants.kTargetRPM,
        // pipe the output to the shooter motor
        output -> shooterControl.runShooter((output / ShooterConstants.kMaxRPM) + shooterControl.getFeedForward(ShooterConstants.kTargetRPM))),
      new SequentialCommandGroup(
        new WaitForShooter(shooterControl),
        new ParallelCommandGroup(
          new InstantCommand(indexerControl::runIndexer, indexerControl),
          new InstantCommand(shooterControl::runFeed))),
      new SequentialCommandGroup(
        new WaitForExit(shooterControl),
        new InstantCommand(intakeControl::startIntake, intakeControl))))
    .whenReleased(new ParallelCommandGroup(
      new InstantCommand(shooterControl::stopShooter, shooterControl),
      new InstantCommand(indexerControl::stopIndexer, indexerControl),
      new InstantCommand(intakeControl::stopIntake, intakeControl),
      new InstantCommand(shooterControl::stopFeed)
    )); */

    // indexer, feeder, and belts run
    new JoystickButton(drive_Stick, OIConstants.kLeftBumper) // left bumper
      .whenPressed(new InstantCommand(shooterControl::runFeed, shooterControl))
      .whenReleased(new InstantCommand(shooterControl::stopFeed, shooterControl))
      .whenPressed(new InstantCommand(indexerControl::runIndexer))
      .whenReleased(new InstantCommand(indexerControl::stopIndexer));

      /* // not stable
    // feeder/belts preload
    new JoystickButton(drive_Stick, OIConstants.kRightBumper) 
    .whenPressed(new ShooterPreload(shooterControl), true)
    .whenReleased(new InstantCommand(shooterControl::stopFeed, shooterControl));
    */

    // extend hood piston
    new JoystickButton(drive_Stick, OIConstants.kTriangleButton) 
    .whenPressed(new InstantCommand(shooterControl::extendHood));


    // retract hood piston 
    new JoystickButton(drive_Stick, OIConstants.kSquareButton)
    .whenPressed(shooterControl::retractHood); 
  }

  /*Ran via Robot.robotPeriodic */
  public void periodic(){
   shooterControl.printShooterRPM();
   shooterControl.printBB();
   SmartDashboard.putNumber("Left Dist", robotDrive.getDistLeft());
   SmartDashboard.putNumber("Right Dist", robotDrive.getDistRight());
   SmartDashboard.putBoolean("Shooter At Speed", shooterControl.isAtSpeed(shooterControl.getTargetRPM(limelight.getErrorY())));
   SmartDashboard.putNumber("Target RPM", shooterControl.getTargetRPM(limelight.getErrorY()));
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
   // An ExampleCommand will run in autonomous
    return m_autonomousChooser.getSelected();
  }
   
}
