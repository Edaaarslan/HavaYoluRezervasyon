import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.swing.*;
import java.awt.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Asenkron rezervasyon sistemi sınıfı, rezervasyon işlemlerini ve durumlarını yönetir
class AsyncReservationSystem {
    private final Map<String, Boolean> reservations; // Koltuk rezervasyon durumlarını tutan harita
    private final Lock lock = new ReentrantLock(); // İşlemler arasında senkronizasyon sağlamak için kilit

    // Yapıcı metot, rezervasyon durumlarını başlatır
    public AsyncReservationSystem() {
        reservations = new HashMap<>();
        reservations.put("Seat1", false);
        reservations.put("Seat2", false);
        reservations.put("Seat3", false);
    }

    // Belirtilen koltuğun rezervasyon durumunu sorgular
    public boolean queryReservation(String seat) {
        lock.lock();
        try {
            return reservations.get(seat);
        } finally {
            lock.unlock();
        }
    }

    // Belirtilen koltuğu rezerve eder
    public void makeReservation(String seat) {
        lock.lock();
        try {
            if (!reservations.get(seat)) {
                System.out.println(Thread.currentThread().getName() + " reserved " + seat);
                reservations.put(seat, true);
            } else {
                System.out.println(Thread.currentThread().getName() + " failed to reserve " + seat);
            }
        } finally {
            lock.unlock();
        }
    }

    // Belirtilen koltuğun rezervasyonunu iptal eder
    public void cancelReservation(String seat) {
        lock.lock();
        try {
            if (reservations.get(seat)) {
                System.out.println(Thread.currentThread().getName() + " canceled " + seat);
                reservations.put(seat, false);
            }
        } finally {
            lock.unlock();
        }
    }
}

// Rezervasyon durumlarını okuyan asenkron okuyucu sınıf
class AsyncReader implements Runnable {
    private final AsyncReservationSystem system; // Rezervasyon sistemine referans
    private final JLabel statusLabel; // GUI'deki durumu gösteren etiket

    // Yapıcı metot, rezervasyon sistemi ve GUI etiketini alır
    public AsyncReader(AsyncReservationSystem system, JLabel statusLabel) {
        this.system = system;
        this.statusLabel = statusLabel;
    }

    @Override
    public void run() {
        Random random = new Random(); // Rastgele sayı üretici
        String[] seats = {"Seat1", "Seat2", "Seat3"}; // Koltuklar

        for (int i = 0; i < 5; i++) {
            String seat = seats[random.nextInt(seats.length)]; // Rastgele bir koltuk seç
            boolean status = system.queryReservation(seat); // Koltuğun durumunu sorgula
            String message = Thread.currentThread().getName() + " checked " + seat + ": " + (status ? "reserved" : "available");
            System.out.println(message);

            // GUI'deki etiketi güncelle
            SwingUtilities.invokeLater(() -> statusLabel.setText(message));
            try {
                Thread.sleep(random.nextInt(100)); // Rastgele bir süre bekle
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

// Rezervasyonları gerçekleştiren asenkron yazıcı sınıf
class AsyncWriter implements Runnable {
    private final AsyncReservationSystem system; // Rezervasyon sistemine referans
    private final JLabel statusLabel; // GUI'deki durumu gösteren etiket

    // Yapıcı metot, rezervasyon sistemi ve GUI etiketini alır
    public AsyncWriter(AsyncReservationSystem system, JLabel statusLabel) {
        this.system = system;
        this.statusLabel = statusLabel;
    }

    @Override
    public void run() {
        Random random = new Random(); // Rastgele sayı üretici
        String[] seats = {"Seat1", "Seat2", "Seat3"}; // Koltuklar

        for (int i = 0; i < 5; i++) {
            String seat = seats[random.nextInt(seats.length)]; // Rastgele bir koltuk seç
            String message;

            // Rastgele bir işlem gerçekleştir (rezervasyon yap veya iptal et)
            if (random.nextBoolean()) {
                system.makeReservation(seat);
                message = Thread.currentThread().getName() + " tried to reserve " + seat;
            } else {
                system.cancelReservation(seat);
                message = Thread.currentThread().getName() + " tried to cancel " + seat;
            }
            System.out.println(message);

            // GUI'deki etiketi güncelle
            SwingUtilities.invokeLater(() -> statusLabel.setText(message));
            try {
                Thread.sleep(random.nextInt(100)); // Rastgele bir süre bekle
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

// Ana sınıf, Swing arayüzü oluşturur ve iş parçacıklarını başlatır
public class AsyncMain {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Async Reservation System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1));

        // GUI için durum etiketlerini oluştur
        JLabel reader1Status = new JLabel("Reader1 status");
        JLabel reader2Status = new JLabel("Reader2 status");
        JLabel writer1Status = new JLabel("Writer1 status");
        JLabel writer2Status = new JLabel("Writer2 status");

        panel.add(new JLabel("Reservation System Status:"));
        panel.add(reader1Status);
        panel.add(reader2Status);
        panel.add(writer1Status);
        panel.add(writer2Status);

        frame.add(panel);
        frame.setVisible(true);

        AsyncReservationSystem system = new AsyncReservationSystem(); // Rezervasyon sistemini başlat

        // Okuyucu ve yazıcı iş parçacıklarını oluştur ve başlat
        Thread reader1 = new Thread(new AsyncReader(system, reader1Status), "Reader1");
        Thread reader2 = new Thread(new AsyncReader(system, reader2Status), "Reader2");
        Thread writer1 = new Thread(new AsyncWriter(system, writer1Status), "Writer1");
        Thread writer2 = new Thread(new AsyncWriter(system, writer2Status), "Writer2");

        reader1.start();
        reader2.start();
        writer1.start();
        writer2.start();
    }
}