package com.aquapetszone.kmp.config.json

object HomeFeedJson {

    val homeAll = """
            {
              "screen": "HOME",
              "version": 1,
              "sections": [
                {
                  "id": "home_banner",
                  "type": "BANNER",
                  "visible": true,
                  "order": 1,
                  "api": "/v1/home/banners"
                },
                {
                  "id": "featured_products",
                  "type": "PRODUCT_CAROUSEL",
                  "visible": true,
                  "order": 2,
                  "api": "/v1/products/carousel"
                },
                {
                  "id": "featured_products_grid",
                  "type": "PRODUCT_GRID",
                  "visible": true,
                  "order": 3,
                  "api": "/v1/products/grid"
                },{
                  "id": "home_banner",
                  "type": "BANNER",
                  "visible": true,
                  "order": 4,
                  "api": "/v1/home/banners"
                },
                {
                  "id": "featured_products",
                  "type": "PRODUCT_CAROUSEL",
                  "visible": true,
                  "order": 5,
                  "api": "/v1/products/carousel"
                },
                {
                  "id": "featured_products_grid",
                  "type": "PRODUCT_GRID",
                  "visible": true,
                  "order": 6,
                  "api": "/v1/products/grid"
                }

              ]
            }
        """.trimIndent()

    val homeFish = """
            {
              "screen": "HOME",
              "version": 1,
              "sections": [
                {
                  "id": "home_banner",
                  "type": "BANNER",
                  "visible": false,
                  "order": 1,
                  "api": "/v1/home/banners"
                },
                {
                  "id": "featured_products",
                  "type": "PRODUCT_CAROUSEL",
                  "visible": true,
                  "order": 2,
                  "api": "/v1/products/carousel"
                },
                {
                  "id": "featured_products_grid",
                  "type": "PRODUCT_GRID",
                  "visible": true,
                  "order": 3,
                  "api": "/v1/products/grid"
                },{
                  "id": "home_banner",
                  "type": "BANNER",
                  "visible": true,
                  "order": 4,
                  "api": "/v1/home/banners"
                },
                {
                  "id": "featured_products",
                  "type": "PRODUCT_CAROUSEL",
                  "visible": true,
                  "order": 5,
                  "api": "/v1/products/carousel"
                },
                {
                  "id": "featured_products_grid",
                  "type": "PRODUCT_GRID",
                  "visible": true,
                  "order": 6,
                  "api": "/v1/products/grid"
                }

              ]
            }
        """.trimIndent()

    val homePlants = """
            {
              "screen": "HOME",
              "version": 1,
              "sections": [
                {
                  "id": "home_banner",
                  "type": "BANNER",
                  "visible": false,
                  "order": 1,
                  "api": "/v1/home/banners"
                },
                {
                  "id": "featured_products",
                  "type": "PRODUCT_CAROUSEL",
                  "visible": true,
                  "order": 2,
                  "api": "/v1/products/carousel"
                },
                {
                  "id": "featured_products_grid",
                  "type": "PRODUCT_GRID",
                  "visible": true,
                  "order": 3,
                  "api": "/v1/products/grid"
                },{
                  "id": "home_banner",
                  "type": "BANNER",
                  "visible": false,
                  "order": 4,
                  "api": "/v1/home/banners"
                },
                {
                  "id": "featured_products",
                  "type": "PRODUCT_CAROUSEL",
                  "visible": false,
                  "order": 5,
                  "api": "/v1/products/carousel"
                },
                {
                  "id": "featured_products_grid",
                  "type": "PRODUCT_GRID",
                  "visible": true,
                  "order": 6,
                  "api": "/v1/products/grid"
                }

              ]
            }
        """.trimIndent()

    val homeTools = """
            {
              "screen": "HOME",
              "version": 1,
              "sections": [
                {
                  "id": "home_banner",
                  "type": "BANNER",
                  "visible": false,
                  "order": 1,
                  "api": "/v1/home/banners"
                },
                {
                  "id": "featured_products",
                  "type": "PRODUCT_CAROUSEL",
                  "visible": true,
                  "order": 2,
                  "api": "/v1/products/carousel"
                },
                {
                  "id": "featured_products_grid",
                  "type": "PRODUCT_GRID",
                  "visible": true,
                  "order": 3,
                  "api": "/v1/products/grid"
                },{
                  "id": "home_banner",
                  "type": "BANNER",
                  "visible": false,
                  "order": 4,
                  "api": "/v1/home/banners"
                },
                {
                  "id": "featured_products",
                  "type": "PRODUCT_CAROUSEL",
                  "visible": true,
                  "order": 5,
                  "api": "/v1/products/carousel"
                },
                {
                  "id": "featured_products_grid",
                  "type": "PRODUCT_GRID",
                  "visible": true,
                  "order": 6,
                  "api": "/v1/products/grid"
                }

              ]
            }
        """.trimIndent()

}