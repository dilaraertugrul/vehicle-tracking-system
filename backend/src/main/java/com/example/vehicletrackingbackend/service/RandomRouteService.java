package com.example.vehicletrackingbackend.service;

import com.example.vehicletrackingbackend.dto.RandomRoutePoint;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;


@Service
public class RandomRouteService {

    private final Random random =
            new Random();


    // =====================================================
    // BATI TÜRKİYE
    // =====================================================

    private static final List<RandomRoutePoint> WEST_CITIES =
            List.of(

                    new RandomRoutePoint(
                            "İstanbul",
                            41.0082,
                            28.9784
                    ),

                    new RandomRoutePoint(
                            "Tekirdağ",
                            40.9780,
                            27.5110
                    ),

                    new RandomRoutePoint(
                            "Çanakkale",
                            40.1553,
                            26.4142
                    ),

                    new RandomRoutePoint(
                            "Bursa",
                            40.1950,
                            29.0600
                    ),

                    new RandomRoutePoint(
                            "Balıkesir",
                            39.6484,
                            27.8826
                    ),

                    new RandomRoutePoint(
                            "İzmir",
                            38.4237,
                            27.1428
                    ),

                    new RandomRoutePoint(
                            "Manisa",
                            38.6191,
                            27.4289
                    ),

                    new RandomRoutePoint(
                            "Aydın",
                            37.8560,
                            27.8416
                    ),

                    new RandomRoutePoint(
                            "Muğla",
                            37.2153,
                            28.3636
                    )
            );


    // =====================================================
    // ORTA TÜRKİYE
    // =====================================================

    private static final List<RandomRoutePoint> CENTRAL_CITIES =
            List.of(

                    new RandomRoutePoint(
                            "Ankara",
                            39.9334,
                            32.8597
                    ),

                    new RandomRoutePoint(
                            "Eskişehir",
                            39.7767,
                            30.5206
                    ),

                    new RandomRoutePoint(
                            "Konya",
                            37.8746,
                            32.4932
                    ),

                    new RandomRoutePoint(
                            "Aksaray",
                            38.3687,
                            34.0370
                    ),

                    new RandomRoutePoint(
                            "Nevşehir",
                            38.6244,
                            34.7239
                    ),

                    new RandomRoutePoint(
                            "Kırşehir",
                            39.1458,
                            34.1606
                    ),

                    new RandomRoutePoint(
                            "Kayseri",
                            38.7312,
                            35.4787
                    ),

                    new RandomRoutePoint(
                            "Yozgat",
                            39.8181,
                            34.8147
                    ),

                    new RandomRoutePoint(
                            "Sivas",
                            39.7505,
                            37.0150
                    )
            );


    // =====================================================
    // DOĞU TÜRKİYE
    // =====================================================

    private static final List<RandomRoutePoint> EAST_CITIES =
            List.of(

                    new RandomRoutePoint(
                            "Erzurum",
                            39.9043,
                            41.2679
                    ),

                    new RandomRoutePoint(
                            "Erzincan",
                            39.7500,
                            39.5000
                    ),

                    new RandomRoutePoint(
                            "Malatya",
                            38.3552,
                            38.3095
                    ),

                    new RandomRoutePoint(
                            "Elazığ",
                            38.6810,
                            39.2264
                    ),

                    new RandomRoutePoint(
                            "Diyarbakır",
                            37.9144,
                            40.2306
                    ),

                    new RandomRoutePoint(
                            "Şanlıurfa",
                            37.1674,
                            38.7955
                    ),

                    new RandomRoutePoint(
                            "Mardin",
                            37.3212,
                            40.7245
                    ),

                    new RandomRoutePoint(
                            "Van",
                            38.5012,
                            43.3729
                    ),

                    new RandomRoutePoint(
                            "Batman",
                            37.8812,
                            41.1351
                    )
            );


    // =====================================================
    // BÖLGEDEN RASTGELE ŞEHİR
    // =====================================================

    public RandomRoutePoint getRandomPoint(
            Region region
    ) {

        List<RandomRoutePoint> cities =
                switch (region) {

                    case WEST ->
                            WEST_CITIES;

                    case CENTRAL ->
                            CENTRAL_CITIES;

                    case EAST ->
                            EAST_CITIES;
                };


        return cities.get(
                random.nextInt(
                        cities.size()
                )
        );
    }


    // =====================================================
    // BÖLGELER
    // =====================================================

    public enum Region {

        WEST,

        CENTRAL,

        EAST
    }
}