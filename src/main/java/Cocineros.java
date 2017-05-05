import akka.actor.*;
import scala.concurrent.duration.Duration;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Cocineros {

    public static class Comanda implements Serializable {}

    public static class PlatoACocinar implements Serializable {
        public final String who;
        public PlatoACocinar(String who) {
            this.who = who;
        }
    }

    public static class PlatoCocinado implements Serializable {
        public final String nombre;
        public PlatoCocinado(String nombre) {
            this.nombre = nombre;
        }
    }

    public static class Cocinero extends UntypedActor {
        public String nombre;
        String queAccionVoyARealizar = "";

        public void onReceive(Object message) {
            if (message instanceof PlatoACocinar){
                System.out.println("piensan lo que van a hacer..");
                queAccionVoyARealizar = "toma , se cocinó " + ((PlatoACocinar) message).who;
            }

            else if (message instanceof Comanda){
                // Send the current queAccionVoyARealizar back to the sender
                System.out.println("Si recibo una comanda, le cuento al que me dice en que voy a estar laburando.");
                ActorRef yo = getSelf();
                ActorRef quienMeEnvioElMensaje = getSender();
                quienMeEnvioElMensaje.tell(new PlatoCocinado(queAccionVoyARealizar), yo);
            }

            else unhandled(message);
        }
    }

    public static void main(String[] args) {
        try {
            // Create the 'helloakka' actor system
            final ActorSystem system = ActorSystem.create("helloakka");

            // Create the 'greeter' actor
            final ActorRef cocineroArguinano = system.actorOf(Props.create(Cocinero.class), "carlos");
            final ActorRef concineroTano = system.actorOf(Props.create(Cocinero.class), "tano");


            // Create the "actor-in-a-box"
            System.out.println("Creo el Roncal");
            final Inbox roncal = Inbox.create(system);


            // Tell the 'greeter' to change its 'queAccionVoyARealizar' nombre
            System.out.println("Le digo a los cocineros que es lo que deberian hacer..");
            cocineroArguinano.tell(new PlatoACocinar("Bife"), ActorRef.noSender());
            concineroTano.tell(new PlatoACocinar("Pastas"), ActorRef.noSender());

            // Ask the 'greeter for the latest 'queAccionVoyARealizar'
            // Reply should go to the "actor-in-a-box"
            System.out.println("Le digo al roncal que produzca el envio de mensaje");
            roncal.send(cocineroArguinano, new Comanda());
            roncal.send(concineroTano, new Comanda());

            // Wait 5 seconds for the reply with the 'queAccionVoyARealizar' nombre
            System.out.println("Le digo que atiende que  que me devuelva el resultado de las presentaciones.");
            final PlatoCocinado platoCocinadoQueSalePrimero = (PlatoCocinado) roncal.receive(Duration.create(5, TimeUnit.SECONDS));
            System.out.println("platoCocinadoQueSalePrimero: " + platoCocinadoQueSalePrimero.nombre);
            final PlatoCocinado platoCocinadoQueSaleSegundo = (PlatoCocinado) roncal.receive(Duration.create(5, TimeUnit.SECONDS));
            System.out.println("platoCocinadoQueSaleSegundo: " + platoCocinadoQueSaleSegundo.nombre);
            System.out.println("-----------fin---------------");
            // Change the queAccionVoyARealizar and ask for it again
            cocineroArguinano.tell(new PlatoACocinar("Mila Rellena, pero si es jueves"), ActorRef.noSender());
            roncal.send(cocineroArguinano, new Comanda());
            final PlatoCocinado platoCocinado2 = (PlatoCocinado) roncal.receive(Duration.create(5, TimeUnit.SECONDS));
            System.out.println("PlatoCocinado: " + platoCocinado2.nombre);

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
            if (message instanceof PlatoCocinado)
                System.out.println(((PlatoCocinado) message).nombre + " y el printer recibiendo");
        }
    }
}
