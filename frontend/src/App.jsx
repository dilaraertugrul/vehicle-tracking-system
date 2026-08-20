import {
    Fragment,
    useEffect,
    useRef,
    useState
} from "react";

import {
    MapContainer,
    TileLayer,
    Marker,
    Popup,
    Polyline,
    CircleMarker
} from "react-leaflet";

import L from "leaflet";

import "leaflet/dist/leaflet.css";
import "./App.css";


const VEHICLE_PLATES = {
    "CAR-101": "34 ABC 101",
    "CAR-102": "06 DEF 102",
    "CAR-103": "35 XYZ 103"
};


const carIcon = L.divIcon({
    className: "car-marker",
    html: '<div class="car-emoji">🚗</div>',
    iconSize: [40, 40],
    iconAnchor: [20, 20],
    popupAnchor: [0, -20]
});


// =========================================================
// YARDIMCI FONKSİYONLAR
// =========================================================

function toDate(timestamp) {

    if (!timestamp) {
        return null;
    }

    // Java LocalDateTime mikro saniyesini
    // JavaScript'in okuyabileceği hale getirir.
    const normalized =
        timestamp.replace(
            /(\.\d{3})\d+/,
            "$1"
        );

    const date =
        new Date(normalized);

    return Number.isNaN(date.getTime())
        ? null
        : date;
}


function formatTimestamp(timestamp) {

    const date =
        toDate(timestamp);

    if (!date) {
        return "-";
    }

    return date.toLocaleString(
        "tr-TR",
        {
            day: "2-digit",
            month: "2-digit",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit",
            second: "2-digit"
        }
    );
}


function formatDuration(seconds) {

    if (seconds === null) {
        return "-";
    }

    const totalSeconds =
        Math.max(
            0,
            Math.floor(seconds)
        );

    const hours =
        Math.floor(
            totalSeconds / 3600
        );

    const minutes =
        Math.floor(
            (totalSeconds % 3600) / 60
        );

    const secs =
        totalSeconds % 60;

    return `${hours} sa ${minutes} dk ${secs} sn`;
}


function calculateArrivalTime(seconds) {

    if (seconds === null) {
        return "-";
    }

    return new Date(
        Date.now()
        +
        seconds * 1000
    ).toLocaleTimeString(
        "tr-TR",
        {
            hour: "2-digit",
            minute: "2-digit"
        }
    );
}


function formatSpeed(speed) {

    return speed == null
        ? "-"
        : `${Math.round(speed)} km/h`;
}


// Aracın rota üzerindeki en yakın noktasını bulur.
// Kırmızı "gidilen rota" çizgisinde kullanılır.
function findClosestRouteIndex(
    route,
    latitude,
    longitude
) {

    let closestIndex = 0;
    let smallestDistance = Infinity;

    route.forEach(
        ([routeLatitude, routeLongitude], index) => {

            const distance =
                Math.hypot(
                    routeLatitude - latitude,
                    routeLongitude - longitude
                );

            if (distance < smallestDistance) {

                smallestDistance =
                    distance;

                closestIndex =
                    index;
            }
        }
    );

    return closestIndex;
}


// =========================================================
// APP
// =========================================================

function App() {

    const turkeyCenter =
        [39.0, 35.0];


    /*
     * React geliştirme modunda useEffect
     * iki kez çalışabileceği için reset'in
     * iki defa gitmesini engeller.
     */
    const simulationStartedRef =
        useRef(false);


    /*
     * Eski PostgreSQL konum kayıtlarıyla
     * yeni simülasyon kayıtlarını ayırır.
     */
    const simulationStartedAtRef =
        useRef(null);


    const [
        vehicleConfigs,
        setVehicleConfigs
    ] = useState([]);


    const [
        vehicles,
        setVehicles
    ] = useState({});


    const [
        plannedRoutes,
        setPlannedRoutes
    ] = useState({});


    const [
        selectedVehicleId,
        setSelectedVehicleId
    ] = useState("CAR-101");


    const [
        remainingDistance,
        setRemainingDistance
    ] = useState(null);


    const [
        remainingSeconds,
        setRemainingSeconds
    ] = useState(null);


    const [
        error,
        setError
    ] = useState(null);


    // =====================================================
    // SİMÜLASYONU BAŞLAT
    // =====================================================

    useEffect(() => {

        if (simulationStartedRef.current) {
            return;
        }

        simulationStartedRef.current =
            true;


        async function startSimulation() {

            try {

                const response =
                    await fetch(
                        "/api/simulation/reset",
                        {
                            method: "POST"
                        }
                    );


                if (!response.ok) {

                    throw new Error(
                        "Yeni simülasyon oluşturulamadı."
                    );
                }


                const data =
                    await response.json();


                // Bundan eski eventler önceki simülasyona aittir.
                simulationStartedAtRef.current =
                    Date.now();


                const configs =
                    data.map(
                        route => ({

                            id:
                            route.vehicleId,

                            plate:
                                VEHICLE_PLATES[
                                    route.vehicleId
                                    ]
                                ||
                                route.vehicleId,

                            startCity:
                            route.startLocationName,

                            destinationCity:
                            route.destinationLocationName,

                            startLatitude:
                            route.startLatitude,

                            startLongitude:
                            route.startLongitude,

                            destinationLatitude:
                            route.destinationLatitude,

                            destinationLongitude:
                            route.destinationLongitude
                        })
                    );


                /*
                 * Kafka'dan yeni LocationEvent gelene kadar
                 * araçları yeni rotalarının başlangıcında göster.
                 */
                const initialVehicles =
                    Object.fromEntries(

                        configs.map(
                            config => [

                                config.id,

                                {
                                    vehicleId:
                                    config.id,

                                    latitude:
                                    config.startLatitude,

                                    longitude:
                                    config.startLongitude,

                                    speed:
                                        null,

                                    timestamp:
                                        null,

                                    isPlaceholder:
                                        true
                                }
                            ]
                        )
                    );


                setVehicleConfigs(
                    configs
                );

                setVehicles(
                    initialVehicles
                );

                setPlannedRoutes(
                    {}
                );

                setSelectedVehicleId(
                    "CAR-101"
                );

                setRemainingDistance(
                    null
                );

                setRemainingSeconds(
                    null
                );

                setError(
                    null
                );


            } catch (err) {

                console.error(
                    "Simülasyon başlatılamadı:",
                    err
                );

                setError(
                    err.message
                );
            }
        }


        startSimulation();

    }, []);


    // =====================================================
    // PLANLANAN OSRM ROTALARINI AL
    // =====================================================

    useEffect(() => {

        if (vehicleConfigs.length === 0) {
            return;
        }


        async function fetchRoutes() {

            try {

                const results =
                    await Promise.all(

                        vehicleConfigs.map(
                            async config => {

                                const response =
                                    await fetch(

                                        `/api/routes`
                                        +
                                        `?startLatitude=${config.startLatitude}`
                                        +
                                        `&startLongitude=${config.startLongitude}`
                                        +
                                        `&destinationLatitude=${config.destinationLatitude}`
                                        +
                                        `&destinationLongitude=${config.destinationLongitude}`
                                    );


                                if (!response.ok) {

                                    throw new Error(
                                        `${config.id} rotası alınamadı.`
                                    );
                                }


                                const data =
                                    await response.json();


                                /*
                                 * OSRM:
                                 * [longitude, latitude]
                                 *
                                 * Leaflet:
                                 * [latitude, longitude]
                                 */
                                const route =
                                    data.map(
                                        ([longitude, latitude]) => [
                                            latitude,
                                            longitude
                                        ]
                                    );


                                return [
                                    config.id,
                                    route
                                ];
                            }
                        )
                    );


                const routes =
                    Object.fromEntries(
                        results
                    );


                setPlannedRoutes(
                    routes
                );


                /*
                 * Başlangıç koordinatı OSRM yolunun
                 * birkaç metre dışında kalabiliyor.
                 *
                 * Placeholder aracı tam rota başlangıcına koy.
                 */
                setVehicles(
                    previous => {

                        const updated =
                            {
                                ...previous
                            };


                        results.forEach(
                            ([vehicleId, route]) => {

                                if (
                                    updated[vehicleId]?.isPlaceholder
                                    &&
                                    route.length > 0
                                ) {

                                    updated[
                                        vehicleId
                                        ] = {

                                        ...updated[
                                            vehicleId
                                            ],

                                        latitude:
                                            route[0][0],

                                        longitude:
                                            route[0][1]
                                    };
                                }
                            }
                        );


                        return updated;
                    }
                );


            } catch (err) {

                console.error(
                    "Rotalar alınamadı:",
                    err
                );

                setError(
                    err.message
                );
            }
        }


        fetchRoutes();

    }, [vehicleConfigs]);


    // =====================================================
    // KAFKA'DAN DB'YE GELEN GÜNCEL ARAÇ KONUMLARI
    // =====================================================

    useEffect(() => {

        if (vehicleConfigs.length === 0) {
            return;
        }


        async function fetchVehicleLocations() {

            const results =
                await Promise.all(

                    vehicleConfigs.map(
                        async config => {

                            try {

                                const response =
                                    await fetch(
                                        `/api/vehicles/${config.id}/latest`
                                    );


                                if (!response.ok) {

                                    return [
                                        config.id,
                                        null
                                    ];
                                }


                                const data =
                                    await response.json();


                                const eventDate =
                                    toDate(
                                        data.timestamp
                                    );


                                if (!eventDate) {

                                    return [
                                        config.id,
                                        null
                                    ];
                                }


                                /*
                                 * Eski simülasyondan kalan
                                 * PostgreSQL kaydını kullanma.
                                 */
                                if (
                                    simulationStartedAtRef.current
                                    !==
                                    null
                                    &&
                                    eventDate.getTime()
                                    <
                                    simulationStartedAtRef.current
                                ) {

                                    return [
                                        config.id,
                                        null
                                    ];
                                }


                                return [

                                    config.id,

                                    {
                                        ...data,
                                        isPlaceholder:
                                            false
                                    }
                                ];


                            } catch (err) {

                                console.error(
                                    `${config.id} konumu alınamadı:`,
                                    err
                                );

                                return [
                                    config.id,
                                    null
                                ];
                            }
                        }
                    )
                );


            /*
             * Yeni event gelmeyen aracın önceki
             * konumunu veya placeholder'ını koru.
             */
            setVehicles(
                previous => {

                    const updated =
                        {
                            ...previous
                        };


                    results.forEach(
                        ([vehicleId, vehicle]) => {

                            if (vehicle) {

                                updated[
                                    vehicleId
                                    ] = vehicle;
                            }
                        }
                    );


                    return updated;
                }
            );
        }


        // İlk sorgu hemen yapılır.
        fetchVehicleLocations();


        // Daha sonra 2 saniyede bir güncellenir.
        const interval =
            setInterval(
                fetchVehicleLocations,
                2000
            );


        return () =>
            clearInterval(
                interval
            );


    }, [vehicleConfigs]);


    // =====================================================
    // SEÇİLİ ARACIN KALAN MESAFE / SÜRESİ
    // =====================================================

    const selectedConfig =
        vehicleConfigs.find(
            vehicle =>
                vehicle.id === selectedVehicleId
        );


    const selectedVehicle =
        vehicles[
            selectedVehicleId
            ];


    useEffect(() => {

        if (
            !selectedVehicle
            ||
            !selectedConfig
        ) {

            return;
        }


        async function fetchRemainingEstimate() {

            try {

                const response =
                    await fetch(

                        `/api/routes/remaining`
                        +
                        `?latitude=${selectedVehicle.latitude}`
                        +
                        `&longitude=${selectedVehicle.longitude}`
                        +
                        `&destinationLatitude=${selectedConfig.destinationLatitude}`
                        +
                        `&destinationLongitude=${selectedConfig.destinationLongitude}`
                    );


                if (!response.ok) {
                    return;
                }


                const data =
                    await response.json();


                setRemainingDistance(
                    data.distanceMeters
                );


                const newSeconds =
                    Math.round(
                        data.durationSeconds
                    );


                setRemainingSeconds(
                    previous =>

                        previous === null

                            ? newSeconds

                            : Math.min(
                                previous,
                                newSeconds
                            )
                );


            } catch (err) {

                console.error(
                    "Kalan rota bilgisi alınamadı:",
                    err
                );
            }
        }


        fetchRemainingEstimate();


    }, [
        selectedVehicle?.latitude,
        selectedVehicle?.longitude,
        selectedConfig?.destinationLatitude,
        selectedConfig?.destinationLongitude
    ]);


    // =====================================================
    // KALAN SÜRE SAYACI
    // =====================================================

    useEffect(() => {

        const interval =
            setInterval(
                () => {

                    setRemainingSeconds(
                        previous => {

                            if (previous === null) {
                                return null;
                            }

                            return Math.max(
                                0,
                                previous - 1
                            );
                        }
                    );

                },
                1000
            );


        return () =>
            clearInterval(
                interval
            );

    }, []);


    // =====================================================
    // ARAÇ SEÇİMİ
    // =====================================================

    function selectVehicle(vehicleId) {

        setSelectedVehicleId(
            vehicleId
        );

        setRemainingDistance(
            null
        );

        setRemainingSeconds(
            null
        );
    }


    // =====================================================
    // DURUM EKRANLARI
    // =====================================================

    if (error) {

        return (

            <div className="status-message">

                <h2>
                    Backend bağlantı hatası
                </h2>

                <p>
                    {error}
                </p>

            </div>
        );
    }


    if (vehicleConfigs.length === 0) {

        return (

            <div className="status-message">

                <h2>
                    Araç rotaları oluşturuluyor...
                </h2>

            </div>
        );
    }


    if (
        !selectedVehicle
        ||
        !selectedConfig
    ) {

        return (

            <div className="status-message">

                <h2>
                    Araç konumları yükleniyor...
                </h2>

            </div>
        );
    }


    // =====================================================
    // ARAYÜZ
    // =====================================================

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


                {/* ================================================= */}
                {/* SOL PANEL */}
                {/* ================================================= */}

                <section className="vehicle-panel">


                    <div className="panel-header">

                        <h2>
                            Araçlar
                        </h2>


                        <div className="live-badge">

                            <span className="live-dot"/>

                            CANLI

                        </div>

                    </div>


                    {/* ARAÇ SEÇİMİ */}

                    <div className="vehicle-selector-list">

                        {vehicleConfigs.map(
                            config => {

                                const vehicle =
                                    vehicles[
                                        config.id
                                        ];

                                const active =
                                    config.id
                                    ===
                                    selectedVehicleId;


                                return (

                                    <button
                                        key={config.id}

                                        className={
                                            active
                                                ? "vehicle-selector active"
                                                : "vehicle-selector"
                                        }

                                        onClick={
                                            () =>
                                                selectVehicle(
                                                    config.id
                                                )
                                        }
                                    >

                                        <div>

                                            <strong>
                                                {config.plate}
                                            </strong>

                                            <span>
                                                {config.startCity}
                                                {" → "}
                                                {config.destinationCity}
                                            </span>

                                        </div>


                                        <span className="selector-speed">

                                            {
                                                formatSpeed(
                                                    vehicle?.speed
                                                )
                                            }

                                        </span>

                                    </button>
                                );
                            }
                        )}

                    </div>


                    {/* SEÇİLİ ARAÇ */}

                    <div className="selected-vehicle-title">

                        Seçili Araç

                    </div>


                    <div className="vehicle-summary">

                        <div>

                            <span className="summary-label">
                                Araç
                            </span>

                            <strong className="vehicle-id">
                                {selectedConfig.plate}
                            </strong>

                        </div>


                        <div className="speed-box">

                            <span>
                                Hız
                            </span>

                            <strong>

                                {
                                    formatSpeed(
                                        selectedVehicle.speed
                                    )
                                }

                            </strong>

                        </div>

                    </div>


                    {/* KALKIŞ / VARIŞ */}

                    <div className="route-summary">

                        <div>

                            <span>
                                Kalkış
                            </span>

                            <strong>
                                {selectedConfig.startCity}
                            </strong>

                        </div>


                        <div className="route-arrow">
                            →
                        </div>


                        <div>

                            <span>
                                Varış
                            </span>

                            <strong>
                                {selectedConfig.destinationCity}
                            </strong>

                        </div>

                    </div>


                    {/* MESAFE / VARIŞ */}

                    <div className="stats-grid">

                        <div className="stat-card">

                            <span>
                                Kalan Mesafe
                            </span>

                            <strong>

                                {
                                    remainingDistance !== null

                                        ? `${Math.round(
                                            remainingDistance / 1000
                                        )} km`

                                        : "-"
                                }

                            </strong>

                        </div>


                        <div className="stat-card">

                            <span>
                                Tahmini Varış
                            </span>

                            <strong>

                                {
                                    calculateArrivalTime(
                                        remainingSeconds
                                    )
                                }

                            </strong>

                        </div>

                    </div>


                    <div className="countdown-card">

                        <span>
                            Kalan Süre
                        </span>

                        <strong>

                            {
                                formatDuration(
                                    remainingSeconds
                                )
                            }

                        </strong>

                    </div>


                    <div className="last-update">

                        <span>
                            Son güncelleme
                        </span>

                        <strong>

                            {
                                formatTimestamp(
                                    selectedVehicle.timestamp
                                )
                            }

                        </strong>

                    </div>

                </section>


                {/* ================================================= */}
                {/* HARİTA */}
                {/* ================================================= */}

                <section className="map-panel">

                    <MapContainer
                        center={turkeyCenter}
                        zoom={6}
                        scrollWheelZoom={true}
                        className="map"
                    >

                        <TileLayer
                            attribution="&copy; OpenStreetMap contributors"
                            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                        />


                        {vehicleConfigs.map(
                            config => {

                                const route =
                                    plannedRoutes[
                                        config.id
                                        ]
                                    ||
                                    [];


                                const vehicle =
                                    vehicles[
                                        config.id
                                        ];


                                if (route.length === 0) {
                                    return null;
                                }


                                const startPoint =
                                    route[0];


                                const destinationPoint =
                                    route[
                                    route.length - 1
                                        ];


                                let traveledRoute =
                                    [];


                                /*
                                 * Araç Kafka'dan gelen gerçek
                                 * konuma geçtiyse kırmızı geçmiş
                                 * rotayı oluştur.
                                 */
                                if (
                                    vehicle
                                    &&
                                    !vehicle.isPlaceholder
                                ) {

                                    const currentIndex =
                                        findClosestRouteIndex(

                                            route,

                                            vehicle.latitude,

                                            vehicle.longitude
                                        );


                                    traveledRoute =
                                        route.slice(
                                            0,
                                            currentIndex + 1
                                        );
                                }


                                return (

                                    <Fragment
                                        key={config.id}
                                    >


                                        {/* PLANLANAN ROTA */}

                                        <Polyline
                                            positions={route}

                                            pathOptions={{
                                                color: "#2563eb",
                                                weight: 4,
                                                opacity: 0.7
                                            }}
                                        />


                                        {/* GİDİLEN ROTA */}

                                        {
                                            traveledRoute.length > 1
                                            &&
                                            <Polyline
                                                positions={
                                                    traveledRoute
                                                }

                                                pathOptions={{
                                                    color: "#dc2626",
                                                    weight: 5,
                                                    opacity: 0.9
                                                }}
                                            />
                                        }


                                        {/* BAŞLANGIÇ */}

                                        <CircleMarker
                                            center={startPoint}
                                            radius={7}

                                            pathOptions={{
                                                color: "#15803d",
                                                fillColor: "#22c55e",
                                                fillOpacity: 1,
                                                weight: 2
                                            }}
                                        >

                                            <Popup>

                                                <strong>
                                                    {config.startCity}
                                                </strong>

                                                <br/>

                                                {config.plate}

                                                <br/>

                                                Başlangıç Noktası

                                            </Popup>

                                        </CircleMarker>


                                        {/* VARIŞ */}

                                        <Marker
                                            position={
                                                destinationPoint
                                            }
                                        >

                                            <Popup>

                                                <strong>
                                                    {config.destinationCity}
                                                </strong>

                                                <br/>

                                                {config.plate}

                                                <br/>

                                                Varış Noktası

                                            </Popup>

                                        </Marker>


                                        {/* ARAÇ */}

                                        {
                                            vehicle
                                            &&
                                            <Marker
                                                position={[
                                                    vehicle.latitude,
                                                    vehicle.longitude
                                                ]}

                                                icon={
                                                    carIcon
                                                }
                                            >

                                                <Popup>

                                                    <strong>
                                                        {config.plate}
                                                    </strong>

                                                    <br/>

                                                    {config.startCity}
                                                    {" → "}
                                                    {config.destinationCity}

                                                    <br/>

                                                    Hız: {
                                                    formatSpeed(
                                                        vehicle.speed
                                                    )
                                                }

                                                    <br/>

                                                    Son Güncelleme:{" "}

                                                    {
                                                        formatTimestamp(
                                                            vehicle.timestamp
                                                        )
                                                    }

                                                </Popup>

                                            </Marker>
                                        }

                                    </Fragment>
                                );
                            }
                        )}

                    </MapContainer>

                </section>

            </main>

        </div>
    );
}


export default App;