import java.util.Scanner;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

public class ConversorMoneda {

    private static final String API_KEY = "06f919c87d3ae5d869acbfc2";
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/";

    private static final String PESO_ARGENTINO = "ARS";
    private static final String DOLAR = "USD";
    private static final String REAL_BRASILENO = "BRL";

    private static Map<String, Double> tasasDeCambio = new HashMap<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean continuar = true;

        actualizarTasasDeCambio(PESO_ARGENTINO);

        while (continuar) {
            mostrarMenu();
            int opcion = obtenerOpcion(scanner);
            if (opcion == 7) {
                System.out.println("¡Gracias por usar el Conversor de Moneda!");
                continuar = false;
                continue;
            }

            String monedaOrigen;
            String monedaDestino;

            switch (opcion) {
                case 1:
                    monedaOrigen = PESO_ARGENTINO;
                    monedaDestino = DOLAR;
                    break;
                case 2:
                    monedaOrigen = DOLAR;
                    monedaDestino = PESO_ARGENTINO;
                    break;
                case 3:
                    monedaOrigen = PESO_ARGENTINO;
                    monedaDestino = REAL_BRASILENO;
                    break;
                case 4:
                    monedaOrigen = REAL_BRASILENO;
                    monedaDestino = PESO_ARGENTINO;
                    break;
                case 5:
                    monedaOrigen = DOLAR;
                    monedaDestino = REAL_BRASILENO;
                    break;
                case 6:
                    monedaOrigen = REAL_BRASILENO;
                    monedaDestino = DOLAR;
                    break;
                default:
                    System.out.println("Opción no válida. Por favor, elija una opción del 1 al 7.");
                    continue;
            }

            System.out.print("Ingrese el valor que desea convertir: ");
            double valor = obtenerValor(scanner);

            double valorConvertido = convertirMoneda(monedaOrigen, monedaDestino, valor);
            String simboloOrigen = obtenerSimbolo(monedaOrigen);
            String nombreDestino = obtenerNombreMoneda(monedaDestino);

            System.out.printf("El valor %.2f [%s] corresponde al valor final de =>>> %.2f [%s]%n",
                    valor, simboloOrigen, valorConvertido, nombreDestino);
        }

        scanner.close();
    }

    private static void mostrarMenu() {
        System.out.println("****************************************");
        System.out.println("Bienvenido a nuestro Conversor de Moneda");
        System.out.println();
        System.out.println("1) Peso argentino  =>> Dólar");
        System.out.println("2) Dólar =>> Peso Argentino");
        System.out.println("3) Peso argentino  =>> Real brasileño");
        System.out.println("4) Real brasileño =>> Peso argentino");
        System.out.println("5) Dólar =>> Real brasileño");
        System.out.println("6) Real brasileño =>> Dólar");
        System.out.println("7) Salir");
        System.out.println();
        System.out.print("Elija una opción válida: ");
    }

    private static int obtenerOpcion(Scanner scanner) {
        int opcion = -1;
        try {
            opcion = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException ignored) {
        }
        return opcion;
    }

    private static double obtenerValor(Scanner scanner) {
        double valor = 0.0;
        boolean valido = false;
        while (!valido) {
            try {
                valor = Double.parseDouble(scanner.nextLine());
                if (valor < 0) {
                    System.out.print("Por favor, ingrese un valor positivo: ");
                } else {
                    valido = true;
                }
            } catch (NumberFormatException e) {
                System.out.print("Entrada inválida. Ingrese un número válido: ");
            }
        }
        return valor;
    }

    private static double convertirMoneda(String origen, String destino, double valor) {
        if (!tasasDeCambio.containsKey(destino)) {
            actualizarTasasDeCambio(origen);
        }

        Double tasaOrigen = tasasDeCambio.get(origen);
        Double tasaDestino = tasasDeCambio.get(destino);

        System.out.println("Tasa de cambio de " + origen + " a " + destino + ": " + tasaDestino);

        if (tasaOrigen == null || tasaDestino == null) {
            System.out.println("No se pudo obtener la tasa de cambio para " + origen + " o " + destino);
            return valor;
        }

        return valor * (tasaDestino / tasaOrigen);
    }



    private static void actualizarTasasDeCambio(String base) {
        try {
            URL url = new URL(API_URL + base);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();

            Gson gson = new Gson();
            ApiResponse response = gson.fromJson(new InputStreamReader(request.getInputStream()), ApiResponse.class);

            System.out.println("Respuesta de la API: " + response.result);
            System.out.println("Tasas de cambio: " + response.conversion_rates);

            if (response.result.equals("success")) {
                tasasDeCambio = response.conversion_rates;
                System.out.println("Tasas de cambio actualizadas: " + tasasDeCambio);
            } else {
                System.out.println("Error al obtener las tasas de cambio: " + response.error_type);
            }
        } catch (Exception e) {
            System.out.println("Error al conectar con la API: " + e.getMessage());
        }
    }


    private static String obtenerSimbolo(String moneda) {
        return switch (moneda) {
            case PESO_ARGENTINO -> "ARS";
            case DOLAR -> "USD";
            case REAL_BRASILENO -> "BRL";
            default -> "";
        };
    }

    private static String obtenerNombreMoneda(String moneda) {
        return switch (moneda) {
            case PESO_ARGENTINO -> "Peso argentino";
            case DOLAR -> "Dólar";
            case REAL_BRASILENO -> "Real brasileño";
            default -> "";
        };
    }

    static class ApiResponse {
        String result;
        Map<String, Double> conversion_rates;
        String error_type;
    }
}
