import {useEffect, useState} from "react";

import {MapContainer, TileLayer, Marker, Popup, Polyline, CircleMarker} from "react-leaflet";

// Leaflet'in kendi özelliklerini kullanmak için.
import L from "leaflet";

// MapContainer  Haritanın kendisi.
// TileLayer     OpenStreetMap harita görüntüsü.
// Marker        Haritadaki işaretçiler.
// Popup         Marker'a tıklanınca açılan bilgi kutusu.
// Polyline      İstanbul → Malatya rotasını çizmek için.
// CircleMarker  İstanbul başlangıç noktasını göstermek için.

import "leaflet/dist/leaflet.css";
import "./App.css";


const carIcon = L.divIcon({
    className: "car-marker",
    html: '<div class="car-emoji">🚗</div>',
    iconSize: [40, 40],
    iconAnchor: [20, 20],
    popupAnchor: [0, -20]
});

function findClosestRouteIndex(route, latitude, longitude) {
    if (route.length === 0) {
        return 0;
    }

    let closestIndex = 0;
    let smallestDistance = Infinity;

    route.forEach(([routeLatitude, routeLongitude], index) => {

        const distance =
            Math.pow(routeLatitude - latitude, 2) +
            Math.pow(routeLongitude - longitude, 2);

        if (distance < smallestDistance) {
            smallestDistance = distance;
            closestIndex = index;
        }
    });
    // Araca en yakın rota noktasının index'ini döndür.
    return closestIndex;
}
function formatTimestamp(timestamp) {
    const date = new Date(timestamp);
    return date.toLocaleString("tr-TR", {

        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit"
    });
}

function formatDuration(seconds) {
    if (seconds === null) {
        return "-";
    }
    const totalSeconds = Math.max(0, Math.floor(seconds));
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const secs = totalSeconds % 60;

    return `${hours} sa ${minutes} dk ${secs} sn`;
}

function calculateArrivalTime(seconds) { // tahmini
    if (seconds === null) {
        return "-";
    }
    const arrivalDate = new Date(
        Date.now() + seconds * 1000
    );
    return arrivalDate.toLocaleTimeString(
        "tr-TR",
        {
            hour: "2-digit",
            minute: "2-digit"
        }
    );
}

function App() {
    const turkeyCenter = [39.0, 35.0];
    const [plannedRoute, setPlannedRoute] = useState([]);
    const [remainingDistance, setRemainingDistance] = useState(null);
    const [remainingSeconds, setRemainingSeconds] = useState(null);
    const [vehicle, setVehicle] = useState(null); // arabanın güncel konumu
    const [error, setError] = useState(null);     // Backend bağlantı hatasını tutar.

    useEffect(() => {
        const fetchPlannedRoute = async () => {
            try {
                const response = await fetch(
                    "/api/routes/test"
                );
                if (!response.ok) {
                    throw new Error("Planlanan rota alınamadı.");
                }
                const data = await response.json();
                const convertedRoute = data.map(
                    ([longitude, latitude]) => [
                        latitude,
                        longitude
                    ]
                );
                setPlannedRoute(convertedRoute);

            } catch (err) {

                console.error(
                    "Planlanan rota alınırken hata oluştu:",
                    err
                );
            }
        };
        const fetchRemainingEstimate = async (latitude, longitude) => {
            try {

                const response = await fetch(
                    `/api/routes/remaining?latitude=${latitude}&longitude=${longitude}`
                );

                if (!response.ok) {
                    throw new Error("Kalan rota bilgisi alınamadı.");
                }
                const data = await response.json();

                setRemainingDistance(data.distanceMeters);
                setRemainingSeconds(Math.round(data.durationSeconds));

            } catch (err) {

                console.error(
                    "Kalan rota bilgisi alınırken hata oluştu:",
                    err
                );
            }
        };

        const fetchLatestLocation = async () => {

            try {

                const response = await fetch(
                    "/api/vehicles/CAR-101/latest"
                );


                if (!response.ok) {
                    throw new Error("Araç konumu alınamadı.");
                }
                const data = await response.json();
                setVehicle(data); // Aracın en güncel konumunu ekrana gönderiyoruz.
                await fetchRemainingEstimate(
                    data.latitude,
                    data.longitude
                );
                setError(null);
            } catch (err) {
                setError(err.message);
            }
        };

        const initializeTracking = async () => {
            await fetchPlannedRoute();
            await fetchLatestLocation();
        };
        initializeTracking();
        const intervalId = setInterval(
            fetchLatestLocation,
            3000
        );
        return () => {
            clearInterval(intervalId);
        };

    }, []);

    useEffect(() => {

        // Her 1 saniyede bir çalışacak.
        const countdownId = setInterval(() => {

            setRemainingSeconds((previousSeconds) => {

                // OSRM'den henüz veri gelmediyse hiçbir şey yapma.
                if (previousSeconds === null) {
                    return previousSeconds;
                }

                // Sayaç sıfıra ulaştıysa daha fazla azaltma.
                if (previousSeconds <= 0) {
                    return 0;
                }

                return previousSeconds - 1; // Bir saniye azalt.
            });
        }, 1000);
        return () => {
            clearInterval(countdownId);
        };
    }, []);

    if (error) {

        return (
            <div className="status-message">

                <h2>Backend bağlantı hatası</h2>

                <p>{error}</p>
            </div>
        );
    }

    if (!vehicle) {

        return (
            <div className="status-message">

                <h2>Araç konumu yükleniyor...</h2>

            </div>
        );
    }

    // OSRM rotasının ilk koordinatı İstanbul.
    const startPoint =
        plannedRoute.length > 0
            ? plannedRoute[0]
            : null;

    // OSRM rotasının son koordinatı Malatya.
    const destinationPoint =
        plannedRoute.length > 0
            ? plannedRoute[plannedRoute.length - 1]
            : null;

    const currentRouteIndex =
        plannedRoute.length > 0
            ? findClosestRouteIndex(
                plannedRoute,
                vehicle.latitude,
                vehicle.longitude
            )
            : 0;

    const traveledRoute = plannedRoute.slice(
        0,
        currentRouteIndex + 1
    );

    return (

        <div className="app">

            <header className="header">

                <div>

                    <h1>
                        Gerçek Zamanlı Araç Takip Sistemi
                    </h1>

                    <p>
                        Kafka, Spring Boot ve React ile canlı araç konum takibi
                    </p>

                </div>

            </header>

            <main className="dashboard">

                <section className="vehicle-panel">
                    <div className="panel-header">

                        <h2>Araç Bilgileri</h2>

                        {/* Sistemin canlı olarak veri aldığını gösterir */}
                        <div className="live-badge">
                            <span className="live-dot"></span>
                            CANLI
                        </div>

                    </div>
                    <div className="vehicle-summary">

                        <div>

      <span className="summary-label">
        Araç
      </span>

                            <strong className="vehicle-id">
                                {vehicle.vehicleId}
                            </strong>

                        </div>


                        <div className="speed-box">

                            <span>Hız</span>

                            <strong>
                                {vehicle.speed} km/h
                            </strong>

                        </div>

                    </div>
                    <div className="route-summary">

                        <div>

                            <span>Kalkış</span>

                            <strong>
                                İstanbul
                            </strong>

                        </div>
                        <div className="route-arrow">
                            →
                        </div>
                        <div>

                            <span>Varış</span>

                            <strong>
                                Malatya
                            </strong>

                        </div>

                    </div>

                    <div className="stats-grid">


                        <div className="stat-card">
                            <span>Kalan Mesafe</span>

                            <strong>
                                {
                                    remainingDistance !== null
                                        ? `${Math.round(remainingDistance / 1000)} km`
                                        : "-"
                                }
                            </strong>

                        </div>
                        <div className="stat-card">
                            <span>Tahmini Varış</span>

                            <strong>
                                {calculateArrivalTime(remainingSeconds)}
                            </strong>
                        </div>
                    </div>
                    <div className="countdown-card">
                        <span>Kalan Süre</span>
                        <strong>
                            {formatDuration(remainingSeconds)}
                        </strong>

                    </div>
                    <div className="last-update">
                        <span>Son güncelleme</span>

                        <strong>
                            {formatTimestamp(vehicle.timestamp)}
                        </strong>

                    </div>

                </section>
                <section className="map-panel">


                    <MapContainer

                        center={turkeyCenter}

                        zoom={6}

                        // Kullanıcı mouse ile zoom yapabilir.
                        scrollWheelZoom={true}

                        className="map"
                    >

                        <TileLayer
                            attribution="&copy; OpenStreetMap contributors"
                            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                        />
                        {plannedRoute.length > 1 && (

                            <Polyline

                                positions={plannedRoute}

                                pathOptions={{

                                    // Planlanan rota mavi.
                                    color: "#2563eb",

                                    // Çizginin kalınlığı.
                                    weight: 5,

                                    // Biraz transparan.
                                    opacity: 0.7
                                }}
                            />

                        )}

                        {traveledRoute.length > 1 && (

                            <Polyline

                                positions={traveledRoute}

                                pathOptions={{

                                    color: "#dc2626",
                                    weight: 6
                                }}
                            />

                        )}

                        {startPoint && (

                            <CircleMarker

                                center={startPoint}

                                radius={9}

                                pathOptions={{

                                    // Dış çizgi.
                                    color: "#15803d",

                                    // İç renk.
                                    fillColor: "#22c55e",

                                    fillOpacity: 1,

                                    weight: 3
                                }}
                            >

                                <Popup>

                                    <strong>
                                        İstanbul
                                    </strong>

                                    <br/>

                                    Başlangıç Noktası

                                </Popup>

                            </CircleMarker>

                        )}

                        {destinationPoint && (

                            <Marker
                                position={destinationPoint}
                            >

                                <Popup>

                                    <strong>
                                        Malatya
                                    </strong>

                                    <br/>

                                    Varış Noktası

                                </Popup>

                            </Marker>

                        )}

                        <Marker

                            // Aracın güncel koordinatı.
                            position={[
                                vehicle.latitude,
                                vehicle.longitude
                            ]}
                            icon={carIcon}
                        >

                            <Popup>

                                <strong>
                                    {vehicle.vehicleId}
                                </strong>

                                <br/>

                                Hız: {vehicle.speed} km/h

                                <br/>

                                Latitude: {vehicle.latitude}

                                <br/>

                                Longitude: {vehicle.longitude}

                                <br/>

                                Son Güncelleme: {formatTimestamp(vehicle.timestamp)}

                            </Popup>

                        </Marker>


                    </MapContainer>

                </section>

            </main>

        </div>
    );
}

export default App;