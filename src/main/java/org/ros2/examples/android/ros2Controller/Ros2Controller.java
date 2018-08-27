/* Copyright 2016-2017 Esteve Fernandez <esteve@apache.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ros2.examples.android.ros2Controller;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.ros2.rcljava.RCLJava;

import org.ros2.android.activity.ROSActivity;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class Ros2Controller extends ROSActivity {

  private Ros2ControllerNode ros2ControllerNode;
  private TextView directionView;
  private TextView limitView;
  private String topicName = "cmd_vel";
  private EditText topicInput;

  private static String logtag = Ros2Controller.class.getName();

  /** Called when the activity is first created. */
  @Override
  public final void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    topicInput = (EditText)findViewById(R.id.topicText);

    Button inputButton = (Button)findViewById(R.id.inputButton);
    inputButton.setOnClickListener(inputButtonListener);
    directionView = (TextView)findViewById(R.id.directionView);
    limitView = (TextView)findViewById(R.id.limitView);
    Button connectButton = (Button)findViewById(R.id.connectButton);
    connectButton.setOnClickListener(connectListener);

    SeekBar maxSpeedBar = (SeekBar) findViewById(R.id.maxSpeedBar);

    maxSpeedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      int progressChangedValue = 500;

      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
          progressChangedValue = progress;
          limitView.setText(String.valueOf(progressChangedValue));
          double maxLinearVel = progressChangedValue / 100.0;
          ros2ControllerNode.setMaxLinearVel(maxLinearVel);
          ros2ControllerNode.setMaxAngVel(maxLinearVel);
      }

      public void onStartTrackingTouch(SeekBar seekBar) {
          // TODO Auto-generated method stub
      }

      public void onStopTrackingTouch(SeekBar seekBar) {
          Toast.makeText(Ros2Controller.this, "Max speed is updated:" + progressChangedValue,
                  Toast.LENGTH_SHORT).show();
      }
    });

    JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);

    RCLJava.rclJavaInit();

    // Start a ROS2 publisher node
    ros2ControllerNode = new Ros2ControllerNode("teleop_twist_joy", topicName);

    // Once joystick is being move
    joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
      @Override
      public void onMove(int angle, int strength) {
        ros2ControllerNode.setTwistValues(angle,strength);
        directionView.setText(String.format("%.2f",ros2ControllerNode.getLinearVelX()) + " , " + String.format("%.2f",ros2ControllerNode.getAngVelZ()));

        }
    });

    // Publish twist msg every 200 millisecond
    Handler pubRoutine = new Handler();
    Runnable r2=new Runnable() {
      public void run() {
        // publish twist msg
        getExecutor().addNode(ros2ControllerNode);
        ros2ControllerNode.pubTwist();
        getExecutor().removeNode(ros2ControllerNode);
        pubRoutine.postDelayed(this, 200);
      }
    };
    pubRoutine.postDelayed(r2, 200);
  }

  private OnClickListener connectListener = new OnClickListener() {
    public void onClick(final View view) {
      Log.d(logtag, "onClick() called - Reconnect");
      Toast
              .makeText(Ros2Controller.this, "Trying to reconnect",
                      Toast.LENGTH_LONG)
              .show();

      // Kill nodes
      ros2ControllerNode = null;
      // Start a ROS2 publisher node
      ros2ControllerNode = new Ros2ControllerNode("teleop_twist_joy", topicName);

    }
  };

  private OnClickListener inputButtonListener = new OnClickListener() {
    public void onClick(final View view) {
      Log.d(logtag, "onClick() called - Enter");

      topicName = topicInput.getText().toString();
      // Kill nodes
      ros2ControllerNode = null;
      // Start a ROS2 publisher node
      ros2ControllerNode = new Ros2ControllerNode("teleop_twist_joy", topicName);

      Toast
              .makeText(Ros2Controller.this, "Published to a new topic called " + topicName,
                      Toast.LENGTH_LONG)
              .show();
    }
  };
}