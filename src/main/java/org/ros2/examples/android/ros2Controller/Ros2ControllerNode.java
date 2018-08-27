/* Copyright 2017 Esteve Fernandez <esteve@apache.org>
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

import android.util.Log;

import org.ros2.rcljava.node.BaseComposableNode;
import org.ros2.rcljava.publisher.Publisher;

public class Ros2ControllerNode extends BaseComposableNode {

  private double linearVelX = 0.0;
  private double linearVelY = 0.0;
  private double linearVelZ = 0.0;
  private double angVelX = 0.0;
  private double angVelY = 0.0;
  private double angVelZ = 0.0;
  private geometry_msgs.msg.Vector3 linear = new geometry_msgs.msg.Vector3();
  private geometry_msgs.msg.Vector3 angular = new geometry_msgs.msg.Vector3();
  private double maxAngVel = 0.5;
  private double maxLinearVel = 0.5;

  private final String topic;

  private static String logtag = Ros2ControllerNode.class.getName();

  private Publisher<geometry_msgs.msg.Twist> publisher;

  public Ros2ControllerNode(final String name, final String topic) {
    super(name);
    this.topic = topic;
    this.publisher = this.node.<geometry_msgs.msg.Twist>createPublisher(
            geometry_msgs.msg.Twist.class, this.topic);
  }

  public double getLinearVelX(){
    return this.linearVelX;
  }
  public double getAngVelZ(){
    return this.angVelZ;
  }

  public void setTwistValues(int angle, int strength){
    int initAng = 23;
    int deltaAng = 45;
    double maxLinear = this.maxLinearVel;
    double maxAng = this.maxAngVel;
    double linearVel = 0.0;
    double angVel = 0.0;

    if(angle >= initAng + 7*deltaAng && strength >= 25 || angle < initAng && strength >= 25){
      // R
      linearVel = 0.0;
      angVel = -strength/100.0 * maxAng;
    }
    else if(angle >= initAng && angle < initAng + deltaAng && strength >= 25){
      // RF
      linearVel = strength/100.0 * maxLinear;
      angVel = -strength/100.0 * maxAng;
    }
    else if(angle >= initAng + deltaAng && angle < initAng + 2*deltaAng && strength >= 25){
      // F
      linearVel = strength/100.0 * maxLinear;
      angVel = 0.0;
    }
    else if(angle >= initAng + 2*deltaAng && angle < initAng + 3*deltaAng && strength >= 25){
      // LF
      linearVel = strength/100.0 * maxLinear;
      angVel = strength/100.0 * maxAng;
    }
    else if(angle >= initAng + 3*deltaAng && angle < initAng + 4*deltaAng && strength >= 25){
      // L
      linearVel = 0.0;
      angVel = strength/100.0 * maxAng;
    }
    else if(angle >= initAng + 4*deltaAng && angle < initAng + 5*deltaAng && strength >= 25){
      // LB
      linearVel = -strength/100.0 * maxLinear;
      angVel = strength/100.0 * maxAng;
    }
    else if(angle >= initAng + 5*deltaAng && angle < initAng + 6*deltaAng && strength >= 25){
      // B
      linearVel = -strength/100.0 * maxLinear;
      angVel = 0.0;
    }
    else if(angle >= initAng + 6*deltaAng && angle < initAng + 7*deltaAng && strength >= 25){
      // RB
      linearVel = -strength/100.0 * maxLinear;
      angVel = -strength/100.0 * maxAng;
    }
    else if(strength < 25){
      // Stop
      linearVel = 0.0;
      angVel = 0.0;
    }
    this.linearVelX = linearVel;
    this.angVelZ = angVel;
  }
  public void setMaxLinearVel(double maxLinearVelIn){
    this.maxLinearVel = maxLinearVelIn;
  }

  public double getMaxLinearVel(){
    return this.maxLinearVel;
  }

  public void setMaxAngVel(double maxAngVelIn){
    this.maxAngVel = maxAngVelIn;
  }

  public double getMaxAngVel(){
    return this.maxAngVel;
  }

  // Transforms twist speeds (linear and angular) to twist msg.
  public void pubTwist() {
    Log.d(logtag, "publish: (" + this.linearVelX + "," + this.angVelZ + ")");
    geometry_msgs.msg.Twist msg = new geometry_msgs.msg.Twist();
    this.linear.setX(this.linearVelX);
    this.linear.setY(this.linearVelY);
    this.linear.setZ(this.linearVelZ);
    this.angular.setX(this.angVelX);
    this.angular.setY(this.angVelY);
    this.angular.setZ(this.angVelZ);
    msg.setLinear(linear);
    msg.setAngular(angular);
    this.publisher.publish(msg);
  }
}
