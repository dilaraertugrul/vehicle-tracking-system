import { Fragment, useEffect, useState } from "react";

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

const VEHICLE_CONFIGS = [

    {
        id: "CAR-101",
        plate: "34 ABC 101",

        startCity: "İstanbul",
        destinationCity: "Bolu",

        startLatitude: 41.0082,
        startLongitude: 28.9784,

        destinationLatitude: 40.7350,
        destinationLongitude: 31.6066
    },

    {
        id: "CAR-102",
        plate: "06 DEF 102",

        startCity: "Ankara",
        destinationCity: "Konya",

        startLatitude: 39.9334,
        startLongitude: 32.8597,

        destinationLatitude: 37.8746,
        destinationLongitude: 32.4932
    },

    {
        id: "CAR-103",
        plate: "35 XYZ 103",

        startCity: "İzmir",
        destinationCity: "Antalya",

        startLatitude: 38.4237,
        startLongitude: 27.1428,

        destinationLatitude: 36.8969,
        destinationLongitude: 30.7133
    }
];


const carIcon = L.divIcon({

    className: "car-marker",

    html: '<div class="car-emoji">🚗</div>',

    iconSize: [40, 40],

    iconAnchor: [20, 20],

    popupAnchor: [0, -20]
});

function findClosestRouteIndex(
    route,
    latitude,
    longitude
) {

    if (route.length === 0) {
        return 0;
    }


    let closestIndex = 0;

    let smallestDistance = Infinity;


    route.forEach(
        ([routeLatitude, routeLongitude], index) => {

            const distance =

                Math.pow(
                    routeLatitude - latitude,
                    2
                )

                +

                Math.pow(
                    routeLongitude - longitude,
                    2
                );


            if (distance < smallestDistance) {

                smallestDistance = distance;

                closestIndex = index;
            }
        }
    );


    return closestIndex;
}

function formatTimestamp(timestamp) {

    if (!timestamp) {
        return "-";
    }


    const date =
        new Date(timestamp);


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


    const arrivalDate =
        new Date(
            Date.now()
            +
            seconds * 1000
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

    const turkeyCenter =
        [39.0, 35.0];

    const [vehicles, setVehicles] =
        useState({});


    // Her aracın planlanan OSRM rotası.
    const [plannedRoutes, setPlannedRoutes] =
        useState({});


    // Sol panelde detayları gösterilecek araç.
    const [
        selectedVehicleId,
        setSelectedVehicleId
    ] = useState("CAR-101");


    // Yalnızca seçili aracın kalan mesafesi.
    const [
        remainingDistance,
        setRemainingDistance
    ] = useState(null);


    // Yalnızca seçili aracın kalan süresi.
    const [
        remainingSeconds,
        setRemainingSeconds
    ] = useState(null);


    const [error, setError] =
        useState(null);

    useEffect(() => {

        const fetchRoutes = async () => {

            try {

                const routeResults =
                    await Promise.all(

                        VEHICLE_CONFIGS.map(

                            async (config) => {

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
                                 *
                                 * [longitude, latitude]
                                 *
                                 * Leaflet:
                                 *
                                 * [latitude, longitude]
                                 */

                                const convertedRoute =
                                    data.map(

                                        ([longitude, latitude]) => [

                                            latitude,
                                            longitude
                                        ]
                                    );


                                return [

                                    config.id,

                                    convertedRoute
                                ];
                            }
                        )
                    );


                // Array'i object yapısına çeviriyoruz.
                //
                // CAR-101 → rota
                // CAR-102 → rota
                // CAR-103 → rota

                setPlannedRoutes(
                    Object.fromEntries(
                        routeResults
                    )
                );


            } catch (err) {

                console.error(
                    "Rotalar alınırken hata:",
                    err
                );


                setError(
                    err.message
                );
            }
        };


        fetchRoutes();

    }, []);


    useEffect(() => {

        const fetchVehicleLocations =
            async () => {

                try {

                    const vehicleResults =
                        await Promise.all(

                            VEHICLE_CONFIGS.map(

                                async (config) => {

                                    const response =
                                        await fetch(

                                            `/api/vehicles/${config.id}/latest`
                                        );


                                    if (!response.ok) {

                                        throw new Error(
                                            `${config.id} konumu alınamadı.`
                                        );
                                    }


                                    const data =
                                        await response.json();


                                    return [

                                        config.id,

                                        data
                                    ];
                                }
                            )
                        );


                    setVehicles(
                        Object.fromEntries(
                            vehicleResults
                        )
                    );


                    setError(null);


                } catch (err) {

                    console.error(
                        "Araç konumları alınırken hata:",
                        err
                    );


                    setError(
                        err.message
                    );
                }
            };


        fetchVehicleLocations();


        // Sonrasında 2 saniyede bir
        // üç aracın güncel konumunu tekrar al.
        const intervalId =
            setInterval(

                fetchVehicleLocations,

                2000
            );


        return () => {

            clearInterval(
                intervalId
            );
        };

    }, []);

    useEffect(() => {

        const selectedVehicle =
            vehicles[selectedVehicleId];


        const selectedConfig =
            VEHICLE_CONFIGS.find(

                vehicle =>
                    vehicle.id === selectedVehicleId
            );


        if (
            !selectedVehicle
            ||
            !selectedConfig
        ) {

            return;
        }


        const fetchRemainingEstimate =
            async () => {

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

                        throw new Error(
                            "Kalan rota bilgisi alınamadı."
                        );
                    }


                    const data =
                        await response.json();


                    setRemainingDistance(
                        data.distanceMeters
                    );


                    setRemainingSeconds(

                        previousSeconds => {

                            const newSeconds =
                                Math.round(
                                    data.durationSeconds
                                );


                            // İlk veri geldiyse
                            // doğrudan OSRM değerini kullan.
                            if (previousSeconds === null) {

                                return newSeconds;
                            }


                            /*
                             * Sayaç örneğin:
                             *
                             * 52 → 51 → 50
                             *
                             * şeklinde giderken
                             * OSRM tekrar 52 döndürürse
                             * süreyi yukarı çıkarmıyoruz.
                             */
                            return Math.min(
                                previousSeconds,
                                newSeconds
                            );
                        }
                    );


                } catch (err) {

                    console.error(
                        "Kalan rota bilgisi alınırken hata:",
                        err
                    );
                }
            };


        fetchRemainingEstimate();


    }, [
        selectedVehicleId,
        vehicles
    ]);

    useEffect(() => {

        const resetSimulation = async () => {

            try {

                await fetch(
                    "/api/simulation/reset",
                    {
                        method: "POST"
                    }
                );

                console.log(
                    "Simülasyon baştan başlatıldı."
                );

            } catch (error) {

                console.error(
                    "Simülasyon sıfırlanamadı:",
                    error
                );
            }
        };


        resetSimulation();

    }, []);


    // KALAN SÜRE SAYACI
    useEffect(() => {

        const countdownId =
            setInterval(() => {

                setRemainingSeconds(

                    previousSeconds => {

                        if (previousSeconds === null) {

                            return previousSeconds;
                        }


                        if (previousSeconds <= 0) {

                            return 0;
                        }


                        return previousSeconds - 1;
                    }
                );

            }, 1000);


        return () => {

            clearInterval(
                countdownId
            );
        };

    }, []);

// sol panelden araç seç
    const selectVehicle =
        (vehicleId) => {

            setSelectedVehicleId(
                vehicleId
            );


            // Önceki aracın kalan mesafe/süre bilgisi
            // yeni seçilen araçta kısa süreli görünmesin.
            setRemainingDistance(null);

            setRemainingSeconds(null);
        };

    const selectedConfig =
        VEHICLE_CONFIGS.find(

            vehicle =>
                vehicle.id === selectedVehicleId
        );


    const selectedVehicle =
        vehicles[selectedVehicleId];

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


    if (!selectedVehicle) {

        return (

            <div className="status-message">

                <h2>
                    Araç konumları yükleniyor...
                </h2>

            </div>
        );
    }

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

                        <h2>
                            Araçlar
                        </h2>


                        <div className="live-badge">

                            <span className="live-dot"></span>

                            CANLI

                        </div>

                    </div>
                    <div className="vehicle-selector-list">


                        {VEHICLE_CONFIGS.map(config => {

                            const vehicle =
                                vehicles[config.id];


                            const isSelected =
                                config.id
                                ===
                                selectedVehicleId;


                            return (

                                <button

                                    key={config.id}

                                    className={

                                        isSelected

                                            ? "vehicle-selector active"

                                            : "vehicle-selector"
                                    }

                                    onClick={() =>
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
                                            vehicle

                                                ? `${vehicle.speed} km/h`

                                                : "-"
                                        }

                                    </span>


                                </button>
                            );
                        })}


                    </div>
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

                                {selectedVehicle.speed} km/h

                            </strong>

                        </div>


                    </div>
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


                    {/* ===================================== */}
                    {/* KALAN SÜRE */}
                    {/* ===================================== */}

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


                    {/* ===================================== */}
                    {/* SON GÜNCELLEME */}
                    {/* ===================================== */}

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


                {/* ========================================= */}
                {/* HARİTA */}
                {/* ========================================= */}

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


                        {/* ================================= */}
                        {/* ÜÇ ARACIN PLANLANAN ROTASI */}
                        {/* ================================= */}
                        {/*
                            Sol panelde hangi araç seçilirse
                            seçilsin üç mavi rota da görünür.
                        */}

                        {VEHICLE_CONFIGS.map(config => {

                            const route =
                                plannedRoutes[config.id]
                                ||
                                [];


                            if (route.length <= 1) {

                                return null;
                            }


                            return (

                                <Polyline

                                    key={`planned-${config.id}`}

                                    positions={route}

                                    pathOptions={{

                                        color: "#2563eb",

                                        weight: 4,

                                        opacity: 0.7
                                    }}
                                />
                            );
                        })}


                        {/* ================================= */}
                        {/* ÜÇ ARACIN GEÇTİĞİ ROTA */}
                        {/* ================================= */}
                        {/*
                            Her araç için ayrı ayrı
                            başlangıçtan güncel konuma kadar
                            kırmızı çizgi oluşturuyoruz.
                        */}

                        {VEHICLE_CONFIGS.map(config => {

                            const vehicle =
                                vehicles[config.id];


                            const route =
                                plannedRoutes[config.id]
                                ||
                                [];


                            if (
                                !vehicle
                                ||
                                route.length <= 1
                            ) {

                                return null;
                            }


                            // Aracın güncel konumuna
                            // en yakın rota noktasını buluyoruz.
                            const currentRouteIndex =
                                findClosestRouteIndex(

                                    route,

                                    vehicle.latitude,

                                    vehicle.longitude
                                );


                            // Başlangıçtan güncel konuma kadar
                            // olan rotayı alıyoruz.
                            const traveledRoute =
                                route.slice(

                                    0,

                                    currentRouteIndex + 1
                                );


                            if (traveledRoute.length <= 1) {

                                return null;
                            }


                            return (

                                <Polyline

                                    key={`traveled-${config.id}`}

                                    positions={traveledRoute}

                                    pathOptions={{

                                        color: "#dc2626",

                                        weight: 5,

                                        opacity: 0.9
                                    }}
                                />
                            );
                        })}


                        {/* ================================= */}
                        {/* ÜÇ ARACIN BAŞLANGIÇ / VARIŞI */}
                        {/* ================================= */}

                        {VEHICLE_CONFIGS.map(config => {

                            const route =
                                plannedRoutes[config.id]
                                ||
                                [];


                            if (route.length === 0) {

                                return null;
                            }


                            // OSRM rotasının ilk noktası.
                            const startPoint =
                                route[0];


                            // OSRM rotasının son noktası.
                            const destinationPoint =
                                route[
                                route.length - 1
                                    ];


                            return (

                                <Fragment
                                    key={`points-${config.id}`}
                                >


                                    {/* Başlangıç noktası */}

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


                                    {/* Varış noktası */}

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


                                </Fragment>
                            );
                        })}


                        {/* ================================= */}
                        {/* ÜÇ ARAÇ AYNI ANDA HARİTADA */}
                        {/* ================================= */}
                        {/*
                            Sol panelde seçim yapmak
                            buradaki araçları etkilemez.
                        */}

                        {VEHICLE_CONFIGS.map(config => {

                            const vehicle =
                                vehicles[config.id];


                            if (!vehicle) {

                                return null;
                            }


                            return (

                                <Marker

                                    key={`vehicle-${config.id}`}

                                    position={[

                                        vehicle.latitude,

                                        vehicle.longitude
                                    ]}

                                    icon={carIcon}
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


                                        Hız: {vehicle.speed} km/h


                                        <br/>


                                        Son Güncelleme:{" "}


                                        {
                                            formatTimestamp(
                                                vehicle.timestamp
                                            )
                                        }


                                    </Popup>


                                </Marker>
                            );
                        })}


                    </MapContainer>


                </section>


            </main>


        </div>
    );
}


export default App;