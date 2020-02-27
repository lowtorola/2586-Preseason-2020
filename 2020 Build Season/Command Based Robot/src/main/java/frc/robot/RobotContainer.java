/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import frc.robot.Constants.DriveConstants;
import frc.robot.Constants.LimelightConstants;
import frc.robot.Constants.OIConstants;
import frc.robot.commands.DefaultDrive;
import frc.robot.commands.DriveStraightOrTurn;
import frc.robot.commands.FeederPreload;
import frc.robot.commands.LimelightTarget;
import frc.robot.commands.WaitForExit;
import frc.robot.commands.WaitForShooter;
import frc.robot.subsystems.ClimbSubsystem;
import frc.robot.subsystems.DriveSubsystem;
import frc.robot.subsystems.IndexerSubsystem;
import frc.robot.subsystems.IntakeSubsystem;
import frc.robot.subsystems.PneumaticSubsystem;
import frc.robot.subsystems.ShooterSubsystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitUntilCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;

/**
 * This class is where the bulk of the robot should be declared.  Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls).  Instead, the structure of the robot
 * (including subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  // The robot's subsystems and commands are defined here...
  private final DriveSubsystem robotDrive = new DriveSubsystem();
  private final ShooterSubsystem shooterControl = new ShooterSubsystem();
  private final IndexerSubsystem indexerControl = new IndexerSubsystem();
  private final PneumaticSubsystem compControl = new PneumaticSubsystem();
  private final IntakeSubsystem intakeControl = new IntakeSubsystem();
  private final ClimbSubsystem climbControl = new ClimbSubsystem();

  Joystick drive_Stick = new Joystick(OIConstants.kDriveControllerPort);
  SendableChooser<Command> autoChooser = new SendableChooser<>();

  /**
   * The container for the robot.  Contains subsystems, OI devices, and commands.
   */
  public RobotContainer() {
    // Configure the button bindings
    configureButtonBindings();
    autoChooser.setDefaultOption("Straight Drive", new DriveStraightOrTurn(robotDrive, 20, 0));
    robotDrive.setDeadband(DriveConstants.kDriveDeadband);
    robotDrive.setDefaultCommand(
      // A split-stick arcade command, with forward/backward controlled by the left
      // hand, and turning controlled by the right.
      new DefaultDrive(
          robotDrive,
          () -> -drive_Stick.getRawAxis(1),
          () -> drive_Stick.getRawAxis(2)));
    //SmartDashboard.put(shooterControl);
  }

  /**
   * Use this method to define your button->command mappings.  Buttons can be created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a
   * {@link edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {

    new JoystickButton(drive_Stick, OIConstants.kIntakeDeployButton) // share
    .whenPressed(new InstantCommand(intakeControl::deployIntake));

    new JoystickButton(drive_Stick, OIConstants.kIntakeRetractButton) // options
    .whenPressed(new InstantCommand(intakeControl::retractIntake));
 
    new JoystickButton(drive_Stick, OIConstants.kFeederPreloadButton) // central pad
    .whenPressed(new FeederPreload(shooterControl), true)
    .whenReleased(new InstantCommand(shooterControl::stopFeeder, shooterControl));

    new JoystickButton(drive_Stick, OIConstants.kLimelightAimButton)  // right bumper
    .whenHeld(new LimelightTarget(robotDrive), true);

    new JoystickButton(drive_Stick, OIConstants.kClimbPowerButton) // triangle
    .whenPressed(new InstantCommand(climbControl::setWinch))
    .whenReleased(new InstantCommand(climbControl::stopWinch));

    new JoystickButton(drive_Stick, OIConstants.kShooterAutoButton) // X
    .whenPressed(new ParallelCommandGroup(
      new InstantCommand(shooterControl::enable, shooterControl),
      new SequentialCommandGroup(
        new WaitUntilCommand(shooterControl::isAtSpeed),
        new ParallelCommandGroup(
          new InstantCommand(indexerControl::runIndexer, indexerControl),
          new InstantCommand(shooterControl::runFeeder))),
      new SequentialCommandGroup(
        new WaitForExit(shooterControl),
        new InstantCommand(intakeControl::startIntake, intakeControl))))
    .whenReleased(new ParallelCommandGroup(
      new InstantCommand(shooterControl::disable, shooterControl),
      new InstantCommand(indexerControl::stopIndexer, indexerControl),
      new InstantCommand(intakeControl::stopIntake, intakeControl),
      new InstantCommand(shooterControl::stopFeeder)
    ));
  }

  /*Ran via Robot.robotPeriodic */
  public void periodic(){
    shooterControl.printBB();
    shooterControl.printShooterRPM();
  }
  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
   // An ExampleCommand will run in autonomous
    return autoChooser.getSelected();
  }
   
}
