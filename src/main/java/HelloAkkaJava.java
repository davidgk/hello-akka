import akka.actor.*;
import scala.concurrent.duration.Duration;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HelloAkkaJava {
    public static class Greet implements Serializable {}

    public static class WhoToGreet implements Serializable {
        public final String who;
        public WhoToGreet(String who) {
            this.who = who;
        }
    }

    public static class Greeting implements Serializable {
        public final String message;
        public Greeting(String message) {
            this.message = message;
        }
    }

    public static class Greeter extends UntypedActor {
        String greeting = "";

        public void onReceive(Object message) {
            if (message instanceof WhoToGreet){
                System.out.println("Soy amable, preparo el saludo");
                greeting = "hello, " + ((WhoToGreet) message).who;
            }

            else if (message instanceof Greet){
                // Send the current queAccionVoyARealizar back to the sender
                System.out.println("Soy amable, devuelvo el pedido de saludo");
                getSender().tell(new Greeting(greeting), getSelf());
            }

            else unhandled(message);
        }
    }

    public static void main(String[] args) {
        try {
            // Create the 'helloakka' actor system
            final ActorSystem system = ActorSystem.create("helloakka");

            // Create the 'greeter' actor
            final ActorRef actorSaludadorCarlos = system.actorOf(Props.create(Greeter.class), "carlos");
            final ActorRef actorSaludadorEmilio = system.actorOf(Props.create(Greeter.class), "emilio");


            // Create the "actor-in-a-box"
            System.out.println("Creo el Inbox");
            final Inbox inbox = Inbox.create(system);


            // Tell the 'greeter' to change its 'queAccionVoyARealizar' nombre
            System.out.println("Le digo a los actores que es lo que deberian hacer..");
            actorSaludadorCarlos.tell(new WhoToGreet("Emilio"), ActorRef.noSender());
            actorSaludadorEmilio.tell(new WhoToGreet("Carlos"), ActorRef.noSender());

            // Ask the 'greeter for the latest 'queAccionVoyARealizar'
            // Reply should go to the "actor-in-a-box"
            System.out.println("Le digo al inbox que produzca el envio de mensaje");
            inbox.send(actorSaludadorCarlos, new Greet());
            inbox.send(actorSaludadorEmilio, new Greet());

            // Wait 5 seconds for the reply with the 'queAccionVoyARealizar' nombre
            System.out.println("Le digo al inbox que me devuelva el resultado de las presentaciones.");
            final Greeting mensajeSaludaCarlosAEmilio = (Greeting) inbox.receive(Duration.create(5, TimeUnit.SECONDS));
            System.out.println("mensajeSaludaCarlosAEmilio: " + mensajeSaludaCarlosAEmilio.message);
            final Greeting mensajeSaludaEmilioACarlos = (Greeting) inbox.receive(Duration.create(5, TimeUnit.SECONDS));
            System.out.println("mensajeSaludaEmilioACarlos: " + mensajeSaludaEmilioACarlos.message);
            System.out.println("-----------fin---------------");
            // Change the queAccionVoyARealizar and ask for it again
            actorSaludadorCarlos.tell(new WhoToGreet("carlos esta saludando"), ActorRef.noSender());
            inbox.send(actorSaludadorCarlos, new Greet());
            final Greeting greeting2 = (Greeting) inbox.receive(Duration.create(5, TimeUnit.SECONDS));
            System.out.println("PlatoCocinado: " + greeting2.message);

            // after zero seconds, send a Comanda nombre every second to the greeter with a sender of the GreetPrinter
//            final ActorRef greetPrinter = system.actorOf(Props.create(GreetPrinter.class));
//            system.scheduler().schedule(Duration.Zero(), Duration.create(1, TimeUnit.SECONDS), actorSaludadorCarlos, new Comanda(), system.dispatcher(), greetPrinter);
        } catch (TimeoutException ex) {
            System.out.println("Got a timeout waiting for reply from an actor");
            ex.printStackTrace();
        }
    }

    public static class GreetPrinter extends UntypedActor {
        public void onReceive(Object message) {
            if (message instanceof Greeting)
                System.out.println(((Greeting) message).message + " y el printer recibiendo");
        }
    }
}
