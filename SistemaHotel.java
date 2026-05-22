import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// PATRÓN SINGLETON
class Registrador {
    private static Registrador instancia;
    private List<String> registros;
    
    private Registrador() {
        registros = new ArrayList<>();
        System.out.println(" Registrador creado");
    }
    
    public static Registrador obtenerInstancia() {
        if (instancia == null) {
            instancia = new Registrador();
        }
        return instancia;
    }
    
    public void registrar(String mensaje) {
        registros.add("[REGISTRO] " + mensaje);
        System.out.println("[REGISTRO] " + mensaje);
    }
}

// PATRÓN OBSERVADOR
interface ObservadorHotel {
    void actualizar(String mensaje);
    String obtenerNombre();
}

class Huesped implements ObservadorHotel {
    private String nombre;
    private String email;
    
    public Huesped(String nombre, String email) {
        this.nombre = nombre;
        this.email = email;
    }
    
    public String obtenerNombre() { return nombre; }
    
    @Override
    public void actualizar(String mensaje) {
        System.out.println("    Notificando a " + nombre + " (" + email + "):");
        System.out.println("      Mensaje: " + mensaje);
    }
}

// SINGLETON + SUJETO OBSERVADOR
class AdministradorHotel {
    private static AdministradorHotel instancia;
    private List<ObservadorHotel> observadores;
    private Map<String, Habitacion> habitaciones;
    private List<Reserva> reservas;
    private int contadorReservas;
    
    private AdministradorHotel() {
        observadores = new ArrayList<>();
        habitaciones = new HashMap<>();
        reservas = new ArrayList<>();
        contadorReservas = 0;
        System.out.println("🔧 AdministradorHotel creado");
    }
    
    public static AdministradorHotel obtenerInstancia() {
        if (instancia == null) {
            instancia = new AdministradorHotel();
        }
        return instancia;
    }
    
    public void suscribir(ObservadorHotel observador) {
        observadores.add(observador);
        System.out.println(" " + observador.obtenerNombre() + " se suscribió");
    }
    
    private void notificarATodos(String mensaje) {
        if (!observadores.isEmpty()) {
            System.out.println("\n Notificando a " + observadores.size() + " suscriptores:");
            for (ObservadorHotel observador : observadores) {
                observador.actualizar(mensaje);
            }
        }
    }
    
    public void agregarHabitacion(Habitacion habitacion) {
        habitaciones.put(habitacion.obtenerNumero(), habitacion);
        Registrador.obtenerInstancia().registrar("Habitación " + habitacion.obtenerNumero() + " agregada");
    }
    
    public void registrarReserva(Reserva reserva) {
        reserva.obtenerHabitacion().establecerDisponible(false);
        reservas.add(reserva);
        Registrador.obtenerInstancia().registrar("Reserva creada para: " + reserva.obtenerHuesped().obtenerNombre());
        System.out.println("\n Reserva registrada:");
        System.out.println("   " + reserva.obtenerDescripcion());
        System.out.println("    Costo total: $" + reserva.obtenerCostoTotal());
    }
    
    public void cambiarEstadoReserva(String idReserva, String nuevoEstado) {
        for (Reserva reserva : reservas) {
            if (reserva.obtenerId().equals(idReserva)) {
                String estadoAnterior = reserva.obtenerEstado();
                reserva.establecerEstado(nuevoEstado);
                Registrador.obtenerInstancia().registrar("Reserva " + idReserva + " cambió a " + nuevoEstado);
                notificarATodos("Su reserva " + idReserva + " cambió: " + estadoAnterior + " → " + nuevoEstado);
                break;
            }
        }
    }
    
    public String generarIdReserva() {
        return "RES-" + (++contadorReservas);
    }
}

class Habitacion {
    private String numero;
    private String tipo;
    private double precioBase;
    private boolean disponible;
    
    public Habitacion(String numero, String tipo, double precioBase) {
        this.numero = numero;
        this.tipo = tipo;
        this.precioBase = precioBase;
        this.disponible = true;
    }
    
    public String obtenerNumero() { return numero; }
    public String obtenerTipo() { return tipo; }
    public double obtenerPrecioBase() { return precioBase; }
    public boolean estaDisponible() { return disponible; }
    public void establecerDisponible(boolean disponible) { this.disponible = disponible; }
}

// PATRÓN DECORADOR
interface Reserva {
    String obtenerId();
    String obtenerDescripcion();
    double obtenerCostoTotal();
    Huesped obtenerHuesped();
    Habitacion obtenerHabitacion();
    String obtenerEstado();
    void establecerEstado(String estado);
}

class ReservaBasica implements Reserva {
    private String id;
    private Huesped huesped;
    private Habitacion habitacion;
    private int noches;
    private String estado;
    
    public ReservaBasica(Huesped huesped, Habitacion habitacion, int noches) {
        this.id = AdministradorHotel.obtenerInstancia().generarIdReserva();
        this.huesped = huesped;
        this.habitacion = habitacion;
        this.noches = noches;
        this.estado = "PENDIENTE";
    }
    
    public String obtenerId() { return id; }
    public Huesped obtenerHuesped() { return huesped; }
    public Habitacion obtenerHabitacion() { return habitacion; }
    public String obtenerEstado() { return estado; }
    public void establecerEstado(String estado) { this.estado = estado; }
    
    public String obtenerDescripcion() {
        return habitacion.obtenerTipo() + " por " + noches + " noches";
    }
    
    public double obtenerCostoTotal() {
        return habitacion.obtenerPrecioBase() * noches;
    }
}

abstract class DecoradorReserva implements Reserva {
    protected Reserva reservaEnvuelta;
    
    public DecoradorReserva(Reserva reservaEnvuelta) {
        this.reservaEnvuelta = reservaEnvuelta;
    }
    
    public String obtenerId() { return reservaEnvuelta.obtenerId(); }
    public Huesped obtenerHuesped() { return reservaEnvuelta.obtenerHuesped(); }
    public Habitacion obtenerHabitacion() { return reservaEnvuelta.obtenerHabitacion(); }
    public String obtenerEstado() { return reservaEnvuelta.obtenerEstado(); }
    public void establecerEstado(String estado) { reservaEnvuelta.establecerEstado(estado); }
    public abstract String obtenerDescripcion();
    public abstract double obtenerCostoTotal();
}

class DesayunoDecorador extends DecoradorReserva {
    private static final double COSTO_DESAYUNO = 15.0;
    
    public DesayunoDecorador(Reserva reservaEnvuelta) {
        super(reservaEnvuelta);
    }
    
    public String obtenerDescripcion() {
        return reservaEnvuelta.obtenerDescripcion() + " + Desayuno Buffet";
    }
    
    public double obtenerCostoTotal() {
        return reservaEnvuelta.obtenerCostoTotal() + COSTO_DESAYUNO;
    }
}

class SpaDecorador extends DecoradorReserva {
    private static final double COSTO_SPA = 30.0;
    
    public SpaDecorador(Reserva reservaEnvuelta) {
        super(reservaEnvuelta);
    }
    
    public String obtenerDescripcion() {
        return reservaEnvuelta.obtenerDescripcion() + " + Acceso al Spa";
    }
    
    public double obtenerCostoTotal() {
        return reservaEnvuelta.obtenerCostoTotal() + COSTO_SPA;
    }
}

class SalidaTardiaDecorador extends DecoradorReserva {
    private static final double COSTO_SALIDA_TARDIA = 10.0;
    
    public SalidaTardiaDecorador(Reserva reservaEnvuelta) {
        super(reservaEnvuelta);
    }
    
    public String obtenerDescripcion() {
        return reservaEnvuelta.obtenerDescripcion() + " + Salida Tardía (hasta 2pm)";
    }
    
    public double obtenerCostoTotal() {
        return reservaEnvuelta.obtenerCostoTotal() + COSTO_SALIDA_TARDIA;
    }
}

// CLASE PRINCIPAL
public class SistemaHotel {
    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("SISTEMA DE RESERVAS DE HOTEL");
        System.out.println("Patrones: Singleton | Decorador | Observador");
        System.out.println("=".repeat(60) + "\n");
        
        Registrador registrador = Registrador.obtenerInstancia();
        registrador.registrar("Iniciando sistema");
        
        AdministradorHotel hotel = AdministradorHotel.obtenerInstancia();
        
        System.out.println("\n--- CREANDO HABITACIONES ---");
        Habitacion habitacion1 = new Habitacion("101", "Estándar", 80.0);
        Habitacion habitacion2 = new Habitacion("202", "Deluxe", 120.0);
        Habitacion habitacion3 = new Habitacion("303", "Suite", 200.0);
        hotel.agregarHabitacion(habitacion1);
        hotel.agregarHabitacion(habitacion2);
        hotel.agregarHabitacion(habitacion3);
        
        System.out.println("\n--- DEMOSTRACIÓN DEL PATRÓN OBSERVADOR ---");
        Huesped ana = new Huesped("Ana García", "ana@email.com");
        Huesped carlos = new Huesped("Carlos López", "carlos@email.com");
        Huesped maria = new Huesped("María Fernández", "maria@email.com");
        hotel.suscribir(ana);
        hotel.suscribir(carlos);
        hotel.suscribir(maria);
        
        System.out.println("\n--- DEMOSTRACIÓN DEL PATRÓN DECORADOR ---");
        
        Reserva reservaBase = new ReservaBasica(ana, habitacion1, 3);
        System.out.println("\nReserva base:");
        System.out.println("   Descripción: " + reservaBase.obtenerDescripcion());
        System.out.println("   Costo: $" + reservaBase.obtenerCostoTotal());
        
        Reserva conDesayuno = new DesayunoDecorador(reservaBase);
        System.out.println("\n+ Con Desayuno:");
        System.out.println("   Descripción: " + conDesayuno.obtenerDescripcion());
        System.out.println("   Costo: $" + conDesayuno.obtenerCostoTotal());
        
        Reserva conDesayunoYSpa = new SpaDecorador(conDesayuno);
        System.out.println("\n+ Con Desayuno + Spa:");
        System.out.println("   Descripción: " + conDesayunoYSpa.obtenerDescripcion());
        System.out.println("   Costo: $" + conDesayunoYSpa.obtenerCostoTotal());
        
        Reserva conTodosLosExtras = new SalidaTardiaDecorador(conDesayunoYSpa);
        System.out.println("\n+ Con TODOS los extras (Desayuno + Spa + Salida Tardía):");
        System.out.println("   Descripción: " + conTodosLosExtras.obtenerDescripcion());
        System.out.println("   Costo TOTAL: $" + conTodosLosExtras.obtenerCostoTotal());
        
        hotel.registrarReserva(conTodosLosExtras);
        
        System.out.println("\n--- CAMBIO DE ESTADO (OBSERVADOR EN ACCIÓN) ---");
        hotel.cambiarEstadoReserva("RES-1", "CONFIRMADA");
        hotel.cambiarEstadoReserva("RES-1", "PAGADA");
        hotel.cambiarEstadoReserva("RES-1", "ENTREGADA");
        
        System.out.println("\n--- RESERVA ADICIONAL ---");
        Reserva otraReserva = new ReservaBasica(carlos, habitacion2, 5);
        Reserva otraConExtras = new SpaDecorador(new DesayunoDecorador(otraReserva));
        hotel.registrarReserva(otraConExtras);
        
        System.out.println("\n--- VERIFICACIÓN DEL PATRÓN SINGLETON ---");
        AdministradorHotel otroHotel = AdministradorHotel.obtenerInstancia();
        System.out.println("¿Misma instancia de AdministradorHotel? " + (hotel == otroHotel));
        
        Registrador otroRegistrador = Registrador.obtenerInstancia();
        System.out.println("¿Misma instancia de Registrador? " + (registrador == otroRegistrador));
        
        System.out.println("\n SISTEMA DE RESERVAS DE HOTEL FINALIZADO CORRECTAMENTE");
    }
}