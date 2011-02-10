/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.imagejdev.sandbox.other.bordello;



public class HelloClient {

    public void greetFormally(String surname) {
        Bordello.get(HelloService.class).sayHello("Sir " + surname);
    }

//    @Test
//    public void testGreeting() {
//        HelloService mockHelloService = new HelloService() {
//
//            public void sayHello(String name) {
//                assertEquals("Sir Smith", name);
//            }
//        };
//        Bordello.set(HelloService.class, mockHelloService);
//        new HelloClient().greetFormally("Smith");
//    }

    public static void main(String[] args) {
        (new HelloClient()).greetFormally("GBH");
    }
}
