/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imagejdev.sandbox.other.bordello;

    @Implementor(HelloServiceImpl.class)
    public interface HelloService {
      public void sayHello(String name);
    }
    