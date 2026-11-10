package com.dolcegelato.backend.config;

import com.dolcegelato.backend.model.*;
import com.dolcegelato.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

// Carga la carta real de Dolce Gelato y Dolce Burger (Carta 2026) la primera vez que arranca.
// Solo se ejecuta si la tabla de categorías está vacía.
// Fuentes: carta oficial 2026 y cuestionario respondido por el dueño (noviembre 2026).
// Lo que no estaba detallado lo definió el equipo según la carta (marcado "DEFINIDO POR EL EQUIPO").
@Component
@RequiredArgsConstructor
public class InicializadorCarta implements CommandLineRunner {

    // RN-01: horarios de cada línea
    private static final LocalTime GELATO_INICIO = LocalTime.of(10, 0);
    private static final LocalTime GELATO_FIN = LocalTime.of(22, 0);
    private static final LocalTime BURGER_INICIO = LocalTime.of(18, 30);
    private static final LocalTime BURGER_FIN = LocalTime.of(22, 0);

    // Frutas confirmadas por el dueño (el admin puede marcar agotadas las de temporada)
    private static final String[] FRUTAS = {"Fresa", "Plátano", "Frutos rojos", "Kiwi", "Mango", "Manzana"};

    // DEFINIDO POR EL EQUIPO: la carta dice "salsas a elección" sin detallar cuáles
    private static final String[] SALSAS = {"Mayonesa", "Ketchup", "Mostaza", "Ají", "Golf", "Tártara"};
    private static final int MAX_SALSAS = 3;

    // De la carta (iced tea e infusiones) + "Canela y clavo", que el negocio ofrece en PedidosYa
    private static final String[] INFUSIONES = {"Tradicional", "Anís", "Digestivo", "Muña", "Durazno",
            "Naranja", "Jamaica", "Canela y clavo"};

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final SaborRepository saborRepository;
    private final ToppingRepository toppingRepository;
    private final OpcionRepository opcionRepository;
    private final ZonaEntregaRepository zonaRepository;
    private final PromocionRepository promocionRepository;

    // Productos y categorías que usan las promociones semanales
    private Categoria heladosArtesanales;
    private Categoria crepesDulces;
    private Producto dolceMilkshake;
    private Producto icedTea;
    private Producto heladoLitro;

    @Override
    public void run(String... args) {
        if (categoriaRepository.count() > 0) {
            return;
        }
        cargarSabores();
        cargarToppings();
        cargarZonas();
        cargarLineaGelato();
        cargarLineaBurger();
        cargarPromociones();

        System.out.println(">>> Carta 2026 cargada: " + categoriaRepository.count() + " categorías, "
                + productoRepository.count() + " productos, " + saborRepository.count() + " sabores, "
                + toppingRepository.count() + " toppings, " + opcionRepository.count() + " opciones");
    }

    // ===================== SABORES Y TOPPINGS =====================

    private void cargarSabores() {
        // Confirmado por el dueño: los sabores de la carta (los 7 de litro + Vainilla, del Café glacé)
        String[] sabores = {"Chocolate", "Chocobrownie", "Coconut", "Cookies and Cream",
                "Lúcuma Brownie", "Menta", "Pie de Limón", "Vainilla"};
        for (String nombre : sabores) {
            Sabor sabor = new Sabor();
            sabor.setNombre(nombre);
            sabor.setDisponible(true);
            saborRepository.save(sabor);
        }
    }

    private void cargarToppings() {
        // Confirmado por el dueño: los toppings de la carta (Fudge, Chocolate líquido, Oreo
        // y la sección "Toppings" a S/ 3). El adicional cuesta S/ 3.00
        topping("Fudge", TipoTopping.LIQUIDO);
        topping("Chocolate líquido", TipoTopping.LIQUIDO);
        topping("Miel", TipoTopping.LIQUIDO);
        topping("Mantequilla de maní", TipoTopping.LIQUIDO);
        topping("Galleta Oreo", TipoTopping.SOLIDO);
        topping("Cacao nibs", TipoTopping.SOLIDO);
        topping("Chía", TipoTopping.SOLIDO);
        topping("Coco", TipoTopping.SOLIDO);
        topping("Granola", TipoTopping.SOLIDO);
    }

    // ===================== ZONAS (EJEMPLO) =====================

    private void cargarZonas() {
        // Confirmado por el dueño: delivery propio en moto eléctrica.
        // Jesús María S/ 5; distritos vecinos hasta S/ 8; pedido mínimo S/ 25 a S/ 30.
        // DEFINIDO POR EL EQUIPO: qué distritos son "vecinos"
        zona("Jesús María", "5.00", "25.00");
        zona("Lince", "8.00", "30.00");
        zona("Pueblo Libre", "8.00", "30.00");
        zona("Breña", "8.00", "30.00");
        zona("San Isidro", "8.00", "30.00");
        zona("Magdalena del Mar", "8.00", "30.00");
    }

    // ===================== DOLCE GELATO (10:00 a 22:00) =====================

    private void cargarLineaGelato() {
        // --- Helados artesanales (RN-03) ---
        // Según el dueño, conos y vasitos no se envían por delivery (se derriten en el traslado)
        Categoria artesanales = categoria("Helados artesanales", Linea.GELATO, 1, 5);
        heladosArtesanales = artesanales;
        soloEnLocal(producto(artesanales, "Helado 1 sabor", "1 sabor de gelato artesanal con topping líquido.", "9.00", 1, 1, 0));
        soloEnLocal(producto(artesanales, "Helado 2 sabores", "2 sabores de gelato artesanal con topping líquido y topping sólido.", "14.00", 2, 1, 1));
        soloEnLocal(producto(artesanales, "Helado 3 sabores", "3 sabores de gelato artesanal con topping líquido y 2 toppings sólidos.", "20.00", 3, 1, 2));

        // --- Helados a la carta ---
        Categoria aLaCarta = categoria("Helados a la carta", Linea.GELATO, 2, 8);
        // Según el dueño, las copas preparadas no se envían por delivery
        Producto cafeGlace = soloEnLocal(simple(aLaCarta, "Café glacé", "Gelato de vainilla o chocolate con dos shots de café.", "17.00"));
        opciones(cafeGlace, "Gelato", true, 1, "Vainilla", "Chocolate");
        soloEnLocal(producto(aLaCarta, "Brownie gelato", "Brownie, 1 bola de gelato artesanal y fudge.", "19.00", 1, 0, 0));
        soloEnLocal(producto(aLaCarta, "Banana split", "3 sabores de gelato artesanal a elección, 1 plátano, 2 toppings y chantilly.", "24.00", 3, 1, 1));
        soloEnLocal(producto(aLaCarta, "Gelato bowl", "3 sabores de gelato artesanal con frutas de estación, chocolate líquido y habano.", "23.00", 3, 0, 0));

        // --- Crepes dulces ---
        crepesDulces = categoria("Crepes dulces", Linea.GELATO, 3, 15);
        producto(crepesDulces, "Crepe simple", "Masa de vainilla y topping líquido.", "16.00", 0, 1, 0);
        Producto crepeFruta = producto(crepesDulces, "Crepe con fruta", "Masa de vainilla, topping líquido y 2 frutas a elección.", "18.00", 0, 1, 0);
        opciones(crepeFruta, "Frutas", true, 2, FRUTAS);
        Producto crepeCreativa = producto(crepesDulces, "Crepe creativa", "Masa de vainilla, topping líquido, 2 frutas a elección y galletas Oreo.", "20.00", 0, 1, 0);
        opciones(crepeCreativa, "Frutas", true, 2, FRUTAS);
        Producto crepeGoloso = producto(crepesDulces, "Crepe goloso", "Masa de vainilla, topping líquido, 2 frutas a elección y 1 bola de gelato artesanal a elección.", "22.00", 1, 1, 0);
        opciones(crepeGoloso, "Frutas", true, 2, FRUTAS);
        producto(crepesDulces, "Crepe especial", "Masa de vainilla, topping líquido, 1 bola de gelato artesanal y galletas Oreo.", "22.00", 1, 1, 0);
        producto(crepesDulces, "Crepe Dolce Gelato", "Masa de vainilla, topping líquido, 2 frutas de estación y 1 bola de gelato artesanal a elección.", "26.00", 1, 1, 0);
        simple(crepesDulces, "Crepe con Nutella", "Masa de vainilla, Nutella, fresa y plátano.", "27.00");

        // --- Crepes salados ---
        Categoria crepesSalados = categoria("Crepes salados", Linea.GELATO, 4, 15);
        simple(crepesSalados, "Crepe mixta", "Masa de crepe, queso, jamón y orégano.", "18.00");
        Producto crepeTropical = simple(crepesSalados, "Crepe tropical", "Masa de crepe, queso, jamón, durazno o piña.", "20.00");
        opciones(crepeTropical, "Fruta", true, 1, "Durazno", "Piña");
        simple(crepesSalados, "Crepe La Pizzera", "Masa de crepe, salsa de tomate, queso, jamón y orégano.", "20.00");

        // --- Waffles ---
        Categoria waffles = categoria("Waffles", Linea.GELATO, 5, 15);
        Producto waffleClasico = producto(waffles, "Waffle clásico", "Waffle, 2 frutas de estación a elección y topping líquido.", "21.00", 0, 1, 0);
        opciones(waffleClasico, "Frutas", true, 2, FRUTAS);
        Producto waffleCasa = producto(waffles, "Waffle de la casa", "Waffle, 2 frutas de estación a elección, topping líquido y 1 bola de gelato a elección.", "24.00", 1, 1, 0);
        opciones(waffleCasa, "Frutas", true, 2, FRUTAS);
        producto(waffles, "Waffle banana split", "Waffle, plátano, 3 sabores de gelato a elección, chantilly y fudge.", "30.00", 3, 0, 0);

        // --- Milkshakes ---
        Categoria milkshakes = categoria("Milkshakes", Linea.GELATO, 6, 5);
        dolceMilkshake = producto(milkshakes, "Dolce milkshake", "Batido de gelato artesanal, crema chantilly, fudge y toppings.", "23.00", 1, 0, 1);
        producto(milkshakes, "Mega Dolce", "Batido de gelato artesanal, brownie, crema chantilly, fudge y toppings.", "29.00", 1, 0, 1);

        // --- Frappés ---
        Categoria frappes = categoria("Frappés", Linea.GELATO, 7, 5);
        simple(frappes, "Frappé de café", null, "21.00");
        simple(frappes, "Frappé moka", null, "21.00");
        simple(frappes, "Frappé de Oreo", null, "22.00");
        simple(frappes, "Frappé de fresa", null, "22.00");
        simple(frappes, "Frappé de mango", null, "22.00");

        // --- Smoothies ---
        Categoria smoothies = categoria("Smoothies", Linea.GELATO, 8, 5);
        simple(smoothies, "Smoothie de arándano", null, "22.00");
        simple(smoothies, "Smoothie de plátano", null, "18.00");
        simple(smoothies, "Smoothie de fresa", null, "20.00");
        simple(smoothies, "Smoothie de mango", null, "20.00");
        simple(smoothies, "Smoothie de frutos rojos", null, "22.00");

        // --- Frozens ---
        Categoria frozens = categoria("Frozens", Linea.GELATO, 9, 5);
        simple(frozens, "Frozen de fresa", null, "19.00");
        simple(frozens, "Frozen de mango", null, "18.00");
        simple(frozens, "Frozen de maracuyá", null, "18.00");
        simple(frozens, "Frozen de piña", null, "18.00");
        simple(frozens, "Frozen de limón", null, "18.00");
        simple(frozens, "Frozen de arándanos", null, "21.00");
        simple(frozens, "Frozen de frutos rojos", null, "23.00");

        // --- Jugos ---
        Categoria jugos = categoria("Jugos", Linea.GELATO, 10, 5);
        String conAgua = "Licuado de fruta, agua, azúcar y/o edulcorante.";
        String conLeche = "Licuado de fruta, leche, azúcar y/o edulcorante.";
        simple(jugos, "Jugo de fresa", conAgua, "14.00");
        simple(jugos, "Jugo de maracuyá", conAgua, "14.00");
        simple(jugos, "Jugo de mango", conAgua, "14.00");
        simple(jugos, "Jugo de papaya", conAgua, "14.00");
        simple(jugos, "Jugo de piña", conAgua, "14.00");
        simple(jugos, "Jugo de fresa con leche", conLeche, "17.00");
        simple(jugos, "Jugo de mango con leche", conLeche, "17.00");
        simple(jugos, "Jugo de plátano con leche", conLeche, "14.00");

        // --- Bebidas frías ---
        Categoria bebidasFrias = categoria("Bebidas frías", Linea.GELATO, 11, 3);
        icedTea = simple(bebidasFrias, "Iced tea", "Infusión, azúcar y/o edulcorante y hielo.", "12.00");
        opciones(icedTea, "Infusión", true, 1, INFUSIONES);
        simple(bebidasFrias, "Iced coffee", "2 shots de espresso, azúcar y/o edulcorante y hielo.", "13.00");
        simple(bebidasFrias, "Limonada", "Agua, jugo de limón, extracto y azúcar y/o edulcorante.", "11.00");
        simple(bebidasFrias, "Gaseosa", null, "6.90");
        simple(bebidasFrias, "Agua mineral San Mateo", null, "4.00");
        simple(bebidasFrias, "Agua mineral Cielo", null, "3.50");

        // --- Bebidas calientes. Según el dueño, solo las que llevan espuma no van por delivery (RN-07) ---
        Categoria bebidasCalientes = categoria("Bebidas calientes", Linea.GELATO, 12, 5);
        simple(bebidasCalientes, "Café americano", null, "8.00");
        simple(bebidasCalientes, "Espresso", null, "8.00");
        soloEnLocal(simple(bebidasCalientes, "Capuccino", null, "13.00"));
        simple(bebidasCalientes, "Doppio espresso", null, "11.00");
        soloEnLocal(simple(bebidasCalientes, "Mocca", null, "14.00"));
        simple(bebidasCalientes, "Chocolate caliente", null, "13.00");
        Producto infusiones = simple(bebidasCalientes, "Infusiones", null, "6.00");
        opciones(infusiones, "Infusión", true, 1, INFUSIONES);

        // --- Smoothie bowls ---
        Categoria bowls = categoria("Smoothie bowls", Linea.GELATO, 13, 8);
        simple(bowls, "Amore bowl", "Batido de plátano, mango, piña y fresas.", "23.00");
        simple(bowls, "Golden bowl", "Batido de mango, piña, plátano y leche.", "23.00");
        simple(bowls, "Parfait", "Combinación saludable: granola, yogur griego y frutas de estación.", "23.00");
        simple(bowls, "Dragon fruit bowl", "Batido de pitahaya, plátano, fresas y arándano.", "26.00");

        // --- Helados de litro ---
        // Confirmado por el dueño: S/ 45, de 1 a 3 sabores. Es el formato que sí va por delivery
        Categoria litro = categoria("Helados de litro", Linea.GELATO, 14, 5);
        heladoLitro = producto(litro, "Helado de litro", "1 litro de gelato artesanal. Combina de 1 a 3 sabores.", "45.00", 3, 0, 0);
        heladoLitro.setSaboresMinimos(1);
        productoRepository.save(heladoLitro);
    }

    // ===================== DOLCE BURGER (18:30 a 22:00) =====================

    private void cargarLineaBurger() {
        // --- Hamburguesas ---
        Categoria hamburguesas = categoria("Hamburguesas", Linea.BURGER, 20, 20);
        conSalsas(simple(hamburguesas, "Dolce clásica", "Hamburguesa de carne, pan brioche, tomate, lechuga, papas fritas y salsas a elección.", "19.00"));
        conSalsas(simple(hamburguesas, "Cheese burguer", "Hamburguesa de carne, pan brioche, queso danbo, queso cheddar, tomate, lechuga, papas fritas y salsas a elección.", "24.00"));
        conSalsas(simple(hamburguesas, "Dolce royal", "Hamburguesa de carne, pan brioche, queso danbo, huevo frito, tomate, lechuga, papas fritas y salsas a elección.", "25.00"));
        conSalsas(simple(hamburguesas, "Dolce especial", "Hamburguesa de carne, pan brioche, queso danbo, huevo frito, jamón, tomate, lechuga, papas fritas y salsas a elección.", "27.00"));
        conSalsas(simple(hamburguesas, "La hawaiana", "Hamburguesa de carne, pan brioche, queso danbo, piña, jamón, tomate, lechuga, papas fritas y salsas a elección.", "27.00"));
        conSalsas(simple(hamburguesas, "Dolce brava", "Hamburguesa de carne, pan brioche, queso danbo, tocino, jamón, tomate, lechuga, papas fritas y salsas a elección.", "29.00"));
        conSalsas(simple(hamburguesas, "La parrillera", "Hamburguesa de carne, pan brioche, chorizo a las finas hierbas, queso danbo, tocino, jamón, tomate, lechuga, papas fritas y salsas a elección.", "33.00"));

        // --- Sándwiches ---
        Categoria sandwiches = categoria("Sándwiches", Linea.BURGER, 21, 15);
        conSalsas(simple(sandwiches, "Jamón y queso", "Pan de molde, jamón, queso danbo, papas fritas y salsas a elección.", "20.00"));
        conSalsas(simple(sandwiches, "La completa", "Pan de molde, jamón, queso danbo, tocino, papas fritas y salsas a elección.", "21.00"));
        conSalsas(simple(sandwiches, "La tropical", "Pan de molde, jamón, queso danbo, piña, tomate, papas fritas y salsas a elección.", "24.00"));
        conSalsas(simple(sandwiches, "Dolce club", "Pan de molde, trozos de hamburguesa, jamón, queso cheddar, tocino, papas fritas y salsas a elección.", "29.00"));
        conSalsas(simple(sandwiches, "Croissant de pollo", "Croissant, pollo deshilachado, apio, mayonesa, lechuga y salsas a elección.", "22.00"));

        // --- Piqueos ---
        Categoria piqueos = categoria("Piqueos", Linea.BURGER, 22, 20);
        conSalsas(simple(piqueos, "Salchiclásica", "Hot dog frankfurter, papas fritas y salsas a elección.", "19.00"));
        conSalsas(simple(piqueos, "Salchidolce", "Hot dog frankfurter, papas fritas, queso danbo y cheddar derretido, tocino trozado, huevo frito y salsas a elección.", "26.00"));
        conSalsas(simple(piqueos, "Salchiparrillera", "Hot dog frankfurter, papas fritas, hamburguesa en trozos, queso danbo y cheddar derretido, tocino trozado, huevo frito y salsas a elección.", "32.00"));
        Producto alitas = simple(piqueos, "Alitas", "Alitas sazonadas con aderezo Dolce y papas fritas (6 unidades).", "21.00");
        opciones(alitas, "Sabor", true, 1, "Acevichadas", "BBQ", "Maracuyá", "Buffalo", "Crispy");
        Producto pollo = conSalsas(simple(piqueos, "Pollo broaster", "Pollo sazonado con receta especial, empanizado al estilo clásico, papas fritas y salsas a elección.", "26.00"));
        opciones(pollo, "Presa", true, 1, "Pecho", "Pierna");
    }

    // ===================== PROMOCIONES SEMANALES =====================

    private void cargarPromociones() {
        // Confirmadas por el dueño. Los montos no estaban detallados: DEFINIDOS POR EL EQUIPO
        // (el precio del iced tea es el que el negocio usa en PedidosYa)
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Lima"));
        LocalDate fin = hoy.plusDays(90);

        Promocion lunes = promocion("Lunes de 2x1 en helados", "Válido solo en la tienda, en copas y barquillos seleccionados.",
                TipoPromocion.PORCENTAJE, "50", hoy, fin);
        lunes.setCategoria(heladosArtesanales);
        lunes.setDiaSemana(DayOfWeek.MONDAY);
        lunes.setSoloEnTienda(true);
        promocionRepository.save(lunes);

        Promocion martes = promocion("Martes de milkshakes: Dolce milkshake a S/ 20", "Precio especial todos los martes.",
                TipoPromocion.PRECIO_FIJO, "20.00", hoy, fin);
        martes.setProducto(dolceMilkshake);
        martes.setDiaSemana(DayOfWeek.TUESDAY);
        promocionRepository.save(martes);

        Promocion miercoles = promocion("Miércoles de iced tea a S/ 10", "Elige tu infusión favorita.",
                TipoPromocion.PRECIO_FIJO, "10.00", hoy, fin);
        miercoles.setProducto(icedTea);
        miercoles.setDiaSemana(DayOfWeek.WEDNESDAY);
        promocionRepository.save(miercoles);

        Promocion jueves = promocion("Jueves de crepes: 15 % de descuento", "En todos los crepes dulces.",
                TipoPromocion.PORCENTAJE, "15", hoy, fin);
        jueves.setCategoria(crepesDulces);
        jueves.setDiaSemana(DayOfWeek.THURSDAY);
        promocionRepository.save(jueves);

        Promocion litros = promocion("2 helados de litro por S/ 60", "Formato familiar: lleva 2 potes de 1 litro.",
                TipoPromocion.CANTIDAD, "60.00", hoy, fin);
        litros.setProducto(heladoLitro);
        litros.setCantidad(2);
        promocionRepository.save(litros);
    }

    // ===================== MÉTODOS DE APOYO =====================

    // minutos = tiempo aproximado de preparación (DEFINIDO POR EL EQUIPO con lo que dijo el dueño:
    // helados 3 a 5 min, comida caliente 15 a 20 min)
    private Categoria categoria(String nombre, Linea linea, int orden, int minutos) {
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setLinea(linea);
        categoria.setOrden(orden);
        categoria.setMinutosPreparacion(minutos);
        return categoriaRepository.save(categoria);
    }

    // Producto con sabores y toppings (helados, crepes, waffles...)
    private Producto producto(Categoria categoria, String nombre, String descripcion, String precio,
                              int sabores, int toppingsLiquidos, int toppingsSolidos) {
        boolean esBurger = categoria.getLinea() == Linea.BURGER;
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(new BigDecimal(precio));
        producto.setCategoria(categoria);
        producto.setHoraInicio(esBurger ? BURGER_INICIO : GELATO_INICIO);
        producto.setHoraFin(esBurger ? BURGER_FIN : GELATO_FIN);
        producto.setCantidadSabores(sabores);
        producto.setSaboresMinimos(sabores); // por defecto, exactamente esa cantidad
        producto.setToppingsLiquidos(toppingsLiquidos);
        producto.setToppingsSolidos(toppingsSolidos);
        producto.setAptoDelivery(true);
        producto.setActivo(true);
        return productoRepository.save(producto);
    }

    // Producto sin sabores ni toppings
    private Producto simple(Categoria categoria, String nombre, String descripcion, String precio) {
        return producto(categoria, nombre, descripcion, precio, 0, 0, 0);
    }

    private Producto soloEnLocal(Producto producto) {
        producto.setAptoDelivery(false);
        return productoRepository.save(producto);
    }

    private Producto conSalsas(Producto producto) {
        opciones(producto, "Salsas", false, MAX_SALSAS, SALSAS);
        return producto;
    }

    private void opciones(Producto producto, String grupo, boolean obligatorio, int maxElegir, String... nombres) {
        for (String nombre : nombres) {
            Opcion opcion = new Opcion();
            opcion.setProducto(producto);
            opcion.setGrupo(grupo);
            opcion.setNombre(nombre);
            opcion.setObligatorio(obligatorio);
            opcion.setMaxElegir(maxElegir);
            opcion.setDisponible(true);
            opcionRepository.save(opcion);
        }
    }

    private void topping(String nombre, TipoTopping tipo) {
        Topping topping = new Topping();
        topping.setNombre(nombre);
        topping.setTipo(tipo);
        topping.setPrecioExtra(new BigDecimal("3.00"));
        topping.setDisponible(true);
        toppingRepository.save(topping);
    }

    private void zona(String nombre, String costoEnvio, String pedidoMinimo) {
        ZonaEntrega zona = new ZonaEntrega();
        zona.setNombre(nombre);
        zona.setCostoEnvio(new BigDecimal(costoEnvio));
        zona.setPedidoMinimo(new BigDecimal(pedidoMinimo));
        zona.setActiva(true);
        zonaRepository.save(zona);
    }

    // Crea la promoción sin guardarla, para completar sus datos antes
    private Promocion promocion(String titulo, String descripcion, TipoPromocion tipo, String valor,
                                LocalDate inicio, LocalDate fin) {
        Promocion promocion = new Promocion();
        promocion.setTitulo(titulo);
        promocion.setDescripcion(descripcion);
        promocion.setTipo(tipo);
        promocion.setValor(new BigDecimal(valor));
        promocion.setFechaInicio(inicio);
        promocion.setFechaFin(fin);
        promocion.setActiva(true);
        return promocion;
    }
}
