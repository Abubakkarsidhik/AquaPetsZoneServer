package com.aquapetszone.kmp.config

object SampleDummyJson {

        val productCarousal = """
    {
      "carouselId": "featured_products_list",
      "showTitle": true,

      "itemUiModel": {
        "bgColor": {
          "startColor": "#FFF7ED",
          "endColor": "#FEF2F2"
        }
      },

      "title": "Featured Products",

      "titleModel": {
        "title": "Featured Products",
        "titleColor": "#0A2540",
        "action": {
          "type": "CATEGORY",
          "value": "featured"
        }
      },

      "showTitleCard": true,
      "titleCardModel": {
        "title": "Hot deals, hotter savings 🔥",
        "titleColor": "#F54900",
        "subTitle": "Save big on aquarium essentials",
        "subTitleColor": "#CA3500",
        "iconUrl": "https://firebasestorage.googleapis.com/v0/b/urs-system-interactors-2e9e0.appspot.com/o/%F0%9F%8E%81%20(1).png?alt=media&token=d6232579-ce5f-4183-a975-a0527cc043fa"
      },

      "type": "PRODUCT_CAROUSEL",
      "displayOrder": 3,

      "products": [

        {
          "productId": "P1001",
          "sku": "FISH-GOLD-001",
          "name": "Golden Fish",
          "imageUrl": "https://firebasestorage.googleapis.com/v0/b/urs-system-interactors-2e9e0.appspot.com/o/aquaPetsZone%2FImage%20(Goldfish%20Premium%20Quality).png?alt=media&token=e0acdb2e-b1e0-48d1-b7f2-759542ecb914",
          "price": {
            "originalPrice": 499,
            "sellingPrice": 399,
            "currency": "INR"
          },
          "discount": {
            "percentage": 20,
            "label": "20% OFF"
          },
          "rating": {
            "average": 4.6,
            "count": 124
          },
          "availability": "IN_STOCK",
          "badge": {
            "type": "BEST_SELLER",
            "text": "Best Seller"
          },
          "sellingUnit": {
            "type": "Quantity",
            "value": 2,
            "unit": "PC"
          },
          "shopByCategory": "FISH",
          "action": {
            "type": "PRODUCT_DETAIL",
            "value": "P1001"
          }
        },

        {
          "productId": "P1003",
          "sku": "FISH-GUPPY-003",
          "name": "Guppy Fish",
          "imageUrl": "https://firebasestorage.googleapis.com/v0/b/urs-system-interactors-2e9e0.appspot.com/o/aquaPetsZone%2Fguppy.webp?alt=media&token=8325f94f-eea2-4fcb-829e-892d08c484ff",
          "price": {
            "originalPrice": 599,
            "sellingPrice": 499,
            "currency": "INR"
          },
          "discount": {
            "percentage": 17,
            "label": "17% OFF"
          },
          "rating": {
            "average": 4.7,
            "count": 201
          },
          "availability": "IN_STOCK",
          "badge": {
            "type": "FEATURED",
            "text": "Featured"
          },
          "sellingUnit": {
            "type": "Quantity",
            "value": 10,
            "unit": "PAIR"
          },
          "shopByCategory": "FISH",
          "action": {
            "type": "PRODUCT_DETAIL",
            "value": "P1003"
          }
        },

        {
          "productId": "P2001",
          "sku": "FOOD-FISH-ADULT-1KG",
          "name": "Premium Fish Food – Adult",
          "imageUrl": "https://firebasestorage.googleapis.com/v0/b/urs-system-interactors-2e9e0.appspot.com/o/aquaPetsZone%2Faquarium-pellet-fish-feed-stock-photo-48323932.webp?alt=media&token=eec44165-db9e-4afe-96d3-40bf94d039c1",
          "price": {
            "originalPrice": 899,
            "sellingPrice": 749,
            "currency": "INR"
          },
          "discount": {
            "percentage": 17,
            "label": "17% OFF"
          },
          "rating": {
            "average": 4.5,
            "count": 342
          },
          "availability": "IN_STOCK",
          "badge": {
            "type": "BEST_SELLER",
            "text": "Top Rated"
          },
          "sellingUnit": {
            "type": "Weight",
            "value": 1,
            "unit": "KG"
          },
          "shopByCategory": "FOOD",
          "action": {
            "type": "PRODUCT_DETAIL",
            "value": "P2001"
          }
        },

        {
          "productId": "P20011",
          "sku": "FOOD-FISH-ADULT-1KG",
          "name": "Premium Fish Food – Adult",
          "imageUrl": "https://firebasestorage.googleapis.com/v0/b/urs-system-interactors-2e9e0.appspot.com/o/aquaPetsZone%2Faquarium-pellet-fish-feed-stock-photo-48323932.webp?alt=media&token=eec44165-db9e-4afe-96d3-40bf94d039c1",
          "price": {
            "originalPrice": 899,
            "sellingPrice": 749,
            "currency": "INR"
          },
          "discount": {
            "percentage": 17,
            "label": "17% OFF"
          },
          "rating": {
            "average": 4.5,
            "count": 342
          },
          "availability": "IN_STOCK",
          "badge": {
            "type": "BEST_SELLER",
            "text": "Top Rated"
          },
          "sellingUnit": {
            "type": "Weight",
            "value": 1,
            "unit": "KG"
          },
          "shopByCategory": "FOOD",
          "action": {
            "type": "PRODUCT_DETAIL",
            "value": "P2001"
          }
        },
        {
          "productId": "P10012",
          "sku": "FISH-GOLD-001",
          "name": "Golden Fish",
          "imageUrl": "https://firebasestorage.googleapis.com/v0/b/urs-system-interactors-2e9e0.appspot.com/o/aquaPetsZone%2FImage%20(Goldfish%20Premium%20Quality).png?alt=media&token=e0acdb2e-b1e0-48d1-b7f2-759542ecb914",
          "price": {
            "originalPrice": 499,
            "sellingPrice": 399,
            "currency": "INR"
          },
          "discount": {
            "percentage": 20,
            "label": "20% OFF"
          },
          "rating": {
            "average": 4.6,
            "count": 124
          },
          "availability": "IN_STOCK",
          "badge": {
            "type": "BEST_SELLER",
            "text": "Best Seller"
          },
          "sellingUnit": {
            "type": "Quantity",
            "value": 2,
            "unit": "PC"
          },
          "shopByCategory": "FISH",
          "action": {
            "type": "PRODUCT_DETAIL",
            "value": "P1001"
          }
        },

        {
          "productId": "P10033",
          "sku": "FISH-GUPPY-003",
          "name": "Guppy Fish",
          "imageUrl": "https://firebasestorage.googleapis.com/v0/b/urs-system-interactors-2e9e0.appspot.com/o/aquaPetsZone%2Fguppy.webp?alt=media&token=8325f94f-eea2-4fcb-829e-892d08c484ff",
          "price": {
            "originalPrice": 599,
            "sellingPrice": 499,
            "currency": "INR"
          },
          "discount": {
            "percentage": 17,
            "label": "17% OFF"
          },
          "rating": {
            "average": 4.7,
            "count": 201
          },
          "availability": "IN_STOCK",
          "badge": {
            "type": "FEATURED",
            "text": "Featured"
          },
          "sellingUnit": {
            "type": "Quantity",
            "value": 10,
            "unit": "PAIR"
          },
          "shopByCategory": "FISH",
          "action": {
            "type": "PRODUCT_DETAIL",
            "value": "P1003"
          }
        },

        {
          "productId": "P20014",
          "sku": "FOOD-FISH-ADULT-1KG",
          "name": "Premium Fish Food – Adult",
          "imageUrl": "https://firebasestorage.googleapis.com/v0/b/urs-system-interactors-2e9e0.appspot.com/o/aquaPetsZone%2Faquarium-pellet-fish-feed-stock-photo-48323932.webp?alt=media&token=eec44165-db9e-4afe-96d3-40bf94d039c1",
          "price": {
            "originalPrice": 899,
            "sellingPrice": 749,
            "currency": "INR"
          },
          "discount": {
            "percentage": 17,
            "label": "17% OFF"
          },
          "rating": {
            "average": 4.5,
            "count": 342
          },
          "availability": "IN_STOCK",
          "badge": {
            "type": "BEST_SELLER",
            "text": "Top Rated"
          },
          "sellingUnit": {
            "type": "Weight",
            "value": 1,
            "unit": "KG"
          },
          "shopByCategory": "FOOD",
          "action": {
            "type": "PRODUCT_DETAIL",
            "value": "P2001"
          }
        },

        {
          "productId": "P20015",
          "sku": "FOOD-FISH-ADULT-1KG",
          "name": "Premium Fish Food – Adult",
          "imageUrl": "https://firebasestorage.googleapis.com/v0/b/urs-system-interactors-2e9e0.appspot.com/o/aquaPetsZone%2Faquarium-pellet-fish-feed-stock-photo-48323932.webp?alt=media&token=eec44165-db9e-4afe-96d3-40bf94d039c1",
          "price": {
            "originalPrice": 899,
            "sellingPrice": 749,
            "currency": "INR"
          },
          "discount": {
            "percentage": 17,
            "label": "17% OFF"
          },
          "rating": {
            "average": 4.5,
            "count": 342
          },
          "availability": "IN_STOCK",
          "badge": {
            "type": "BEST_SELLER",
            "text": "Top Rated"
          },
          "sellingUnit": {
            "type": "Weight",
            "value": 1,
            "unit": "KG"
          },
          "shopByCategory": "FOOD",
          "action": {
            "type": "PRODUCT_DETAIL",
            "value": "P2001"
          }
        }


      ],

      "viewAllEnabled": true,
      "viewAllAction": {
        "type": "CATEGORY",
        "value": "featured"
      },

      "tracking": {
        "impressionId": "imp_home_featured_001",
        "source": "HOME"
      }
    }
    """.trimIndent()

        val productGrid = """
    {
      "carouselId": "featured_products_grid",
      "showTitle": true,

      "itemUiModel": {
        "bgColor": {
          "startColor": "#ECFEFF",
          "endColor": "#EFF6FF"
        }
      },

      "title": "Featured Products",

      "titleModel": {
        "title": "Most Popular",
        "titleColor": "#0A2540",
        "action": {
          "type": "CATEGORY",
          "value": "featured"
        }
      },

      "showTitleCard": true,
      "titleCardModel": {
        "title": "Trending aquatic fish 🐠",
        "titleColor": "#0092B8",
        "subTitle": "Fresh stock, amazing colors",
        "subTitleColor": "#007595",
        "iconUrl": "https://firebasestorage.googleapis.com/v0/b/urs-system-interactors-2e9e0.appspot.com/o/aquaPetsZone%2Fimg_water.png?alt=media&token=b62baecf-0cdc-488b-a51f-9cd1de54a12c"
      },

      "type": "PRODUCT_CAROUSEL",
      "displayOrder": 3,

      "products": [

        {
          "productId": "P10016",
          "sku": "FISH-GOLD-001",
          "name": "Golden Fish",
          "imageUrl": "https://firebasestorage.googleapis.com/v0/b/urs-system-interactors-2e9e0.appspot.com/o/aquaPetsZone%2FImage%20(Goldfish%20Premium%20Quality).png?alt=media&token=e0acdb2e-b1e0-48d1-b7f2-759542ecb914",
          "price": {
            "originalPrice": 499,
            "sellingPrice": 399,
            "currency": "INR"
          },
          "discount": {
            "percentage": 20,
            "label": "20% OFF"
          },
          "rating": {
            "average": 4.6,
            "count": 124
          },
          "availability": "IN_STOCK",
          "badge": {
            "type": "BEST_SELLER",
            "text": "Best Seller"
          },
          "sellingUnit": {
            "type": "Quantity",
            "value": 2,
            "unit": "PC"
          },
          "shopByCategory": "FISH",
          "action": {
            "type": "PRODUCT_DETAIL",
            "value": "P1001"
          }
        },

        {
          "productId": "P10037",
          "sku": "FISH-GUPPY-003",
          "name": "Guppy Fish",
          "imageUrl": "https://firebasestorage.googleapis.com/v0/b/urs-system-interactors-2e9e0.appspot.com/o/aquaPetsZone%2Fguppy.webp?alt=media&token=8325f94f-eea2-4fcb-829e-892d08c484ff",
          "price": {
            "originalPrice": 599,
            "sellingPrice": 499,
            "currency": "INR"
          },
          "discount": {
            "percentage": 17,
            "label": "17% OFF"
          },
          "rating": {
            "average": 4.7,
            "count": 201
          },
          "availability": "IN_STOCK",
          "badge": {
            "type": "FEATURED",
            "text": "Featured"
          },
          "sellingUnit": {
            "type": "Quantity",
            "value": 10,
            "unit": "PAIR"
          },
          "shopByCategory": "FISH",
          "action": {
            "type": "PRODUCT_DETAIL",
            "value": "P1003"
          }
        },

        {
          "productId": "P20018",
          "sku": "FOOD-FISH-ADULT-1KG",
          "name": "Premium Fish Food – Adult",
          "imageUrl": "https://firebasestorage.googleapis.com/v0/b/urs-system-interactors-2e9e0.appspot.com/o/aquaPetsZone%2Faquarium-pellet-fish-feed-stock-photo-48323932.webp?alt=media&token=eec44165-db9e-4afe-96d3-40bf94d039c1",
          "price": {
            "originalPrice": 899,
            "sellingPrice": 749,
            "currency": "INR"
          },
          "discount": {
            "percentage": 17,
            "label": "17% OFF"
          },
          "rating": {
            "average": 4.5,
            "count": 342
          },
          "availability": "IN_STOCK",
          "badge": {
            "type": "BEST_SELLER",
            "text": "Top Rated"
          },
          "sellingUnit": {
            "type": "Weight",
            "value": 1,
            "unit": "KG"
          },
          "shopByCategory": "FOOD",
          "action": {
            "type": "PRODUCT_DETAIL",
            "value": "P2001"
          }
        },

        {
          "productId": "P20019",
          "sku": "FOOD-FISH-ADULT-1KG",
          "name": "Premium Fish Food – Adult",
          "imageUrl": "https://firebasestorage.googleapis.com/v0/b/urs-system-interactors-2e9e0.appspot.com/o/aquaPetsZone%2Faquarium-pellet-fish-feed-stock-photo-48323932.webp?alt=media&token=eec44165-db9e-4afe-96d3-40bf94d039c1",
          "price": {
            "originalPrice": 899,
            "sellingPrice": 749,
            "currency": "INR"
          },
          "discount": {
            "percentage": 17,
            "label": "17% OFF"
          },
          "rating": {
            "average": 4.5,
            "count": 342
          },
          "availability": "IN_STOCK",
          "badge": {
            "type": "BEST_SELLER",
            "text": "Top Rated"
          },
          "sellingUnit": {
            "type": "Weight",
            "value": 1,
            "unit": "KG"
          },
          "shopByCategory": "FOOD",
          "action": {
            "type": "PRODUCT_DETAIL",
            "value": "P2001"
          }
        },
        {
          "productId": "P100110",
          "sku": "FISH-GOLD-001",
          "name": "Golden Fish",
          "imageUrl": "https://firebasestorage.googleapis.com/v0/b/urs-system-interactors-2e9e0.appspot.com/o/aquaPetsZone%2FImage%20(Goldfish%20Premium%20Quality).png?alt=media&token=e0acdb2e-b1e0-48d1-b7f2-759542ecb914",
          "price": {
            "originalPrice": 499,
            "sellingPrice": 399,
            "currency": "INR"
          },
          "discount": {
            "percentage": 20,
            "label": "20% OFF"
          },
          "rating": {
            "average": 4.6,
            "count": 124
          },
          "availability": "IN_STOCK",
          "badge": {
            "type": "BEST_SELLER",
            "text": "Best Seller"
          },
          "sellingUnit": {
            "type": "Quantity",
            "value": 2,
            "unit": "PC"
          },
          "shopByCategory": "FISH",
          "action": {
            "type": "PRODUCT_DETAIL",
            "value": "P1001"
          }
        },

        {
          "productId": "P100311",
          "sku": "FISH-GUPPY-003",
          "name": "Guppy Fish",
          "imageUrl": "https://firebasestorage.googleapis.com/v0/b/urs-system-interactors-2e9e0.appspot.com/o/aquaPetsZone%2Fguppy.webp?alt=media&token=8325f94f-eea2-4fcb-829e-892d08c484ff",
          "price": {
            "originalPrice": 599,
            "sellingPrice": 499,
            "currency": "INR"
          },
          "discount": {
            "percentage": 17,
            "label": "17% OFF"
          },
          "rating": {
            "average": 4.7,
            "count": 201
          },
          "availability": "IN_STOCK",
          "badge": {
            "type": "FEATURED",
            "text": "Featured"
          },
          "sellingUnit": {
            "type": "Quantity",
            "value": 10,
            "unit": "PAIR"
          },
          "shopByCategory": "FISH",
          "action": {
            "type": "PRODUCT_DETAIL",
            "value": "P1003"
          }
        },

        {
          "productId": "P200112",
          "sku": "FOOD-FISH-ADULT-1KG",
          "name": "Premium Fish Food – Adult",
          "imageUrl": "https://firebasestorage.googleapis.com/v0/b/urs-system-interactors-2e9e0.appspot.com/o/aquaPetsZone%2Faquarium-pellet-fish-feed-stock-photo-48323932.webp?alt=media&token=eec44165-db9e-4afe-96d3-40bf94d039c1",
          "price": {
            "originalPrice": 899,
            "sellingPrice": 749,
            "currency": "INR"
          },
          "discount": {
            "percentage": 17,
            "label": "17% OFF"
          },
          "rating": {
            "average": 4.5,
            "count": 342
          },
          "availability": "IN_STOCK",
          "badge": {
            "type": "BEST_SELLER",
            "text": "Top Rated"
          },
          "sellingUnit": {
            "type": "Weight",
            "value": 1,
            "unit": "KG"
          },
          "shopByCategory": "FOOD",
          "action": {
            "type": "PRODUCT_DETAIL",
            "value": "P2001"
          }
        },

        {
          "productId": "P200113",
          "sku": "FOOD-FISH-ADULT-1KG",
          "name": "Premium Fish Food – Adult",
          "imageUrl": "https://firebasestorage.googleapis.com/v0/b/urs-system-interactors-2e9e0.appspot.com/o/aquaPetsZone%2Faquarium-pellet-fish-feed-stock-photo-48323932.webp?alt=media&token=eec44165-db9e-4afe-96d3-40bf94d039c1",
          "price": {
            "originalPrice": 899,
            "sellingPrice": 749,
            "currency": "INR"
          },
          "discount": {
            "percentage": 17,
            "label": "17% OFF"
          },
          "rating": {
            "average": 4.5,
            "count": 342
          },
          "availability": "IN_STOCK",
          "badge": {
            "type": "BEST_SELLER",
            "text": "Top Rated"
          },
          "sellingUnit": {
            "type": "Weight",
            "value": 1,
            "unit": "KG"
          },
          "shopByCategory": "FOOD",
          "action": {
            "type": "PRODUCT_DETAIL",
            "value": "P2001"
          }
        },{
          "productId": "P100114",
          "sku": "FISH-GOLD-001",
          "name": "Golden Fish",
          "imageUrl": "https://firebasestorage.googleapis.com/v0/b/urs-system-interactors-2e9e0.appspot.com/o/aquaPetsZone%2FImage%20(Goldfish%20Premium%20Quality).png?alt=media&token=e0acdb2e-b1e0-48d1-b7f2-759542ecb914",
          "price": {
            "originalPrice": 499,
            "sellingPrice": 399,
            "currency": "INR"
          },
          "discount": {
            "percentage": 20,
            "label": "20% OFF"
          },
          "rating": {
            "average": 4.6,
            "count": 124
          },
          "availability": "IN_STOCK",
          "badge": {
            "type": "BEST_SELLER",
            "text": "Best Seller"
          },
          "sellingUnit": {
            "type": "Quantity",
            "value": 2,
            "unit": "PC"
          },
          "shopByCategory": "FISH",
          "action": {
            "type": "PRODUCT_DETAIL",
            "value": "P1001"
          }
        },

        {
          "productId": "P100315",
          "sku": "FISH-GUPPY-003",
          "name": "Guppy Fish",
          "imageUrl": "https://firebasestorage.googleapis.com/v0/b/urs-system-interactors-2e9e0.appspot.com/o/aquaPetsZone%2Fguppy.webp?alt=media&token=8325f94f-eea2-4fcb-829e-892d08c484ff",
          "price": {
            "originalPrice": 599,
            "sellingPrice": 499,
            "currency": "INR"
          },
          "discount": {
            "percentage": 17,
            "label": "17% OFF"
          },
          "rating": {
            "average": 4.7,
            "count": 201
          },
          "availability": "IN_STOCK",
          "badge": {
            "type": "FEATURED",
            "text": "Featured"
          },
          "sellingUnit": {
            "type": "Quantity",
            "value": 10,
            "unit": "PAIR"
          },
          "shopByCategory": "FISH",
          "action": {
            "type": "PRODUCT_DETAIL",
            "value": "P1003"
          }
        },

        {
          "productId": "P200116",
          "sku": "FOOD-FISH-ADULT-1KG",
          "name": "Premium Fish Food – Adult",
          "imageUrl": "https://firebasestorage.googleapis.com/v0/b/urs-system-interactors-2e9e0.appspot.com/o/aquaPetsZone%2Faquarium-pellet-fish-feed-stock-photo-48323932.webp?alt=media&token=eec44165-db9e-4afe-96d3-40bf94d039c1",
          "price": {
            "originalPrice": 899,
            "sellingPrice": 749,
            "currency": "INR"
          },
          "discount": {
            "percentage": 17,
            "label": "17% OFF"
          },
          "rating": {
            "average": 4.5,
            "count": 342
          },
          "availability": "IN_STOCK",
          "badge": {
            "type": "BEST_SELLER",
            "text": "Top Rated"
          },
          "sellingUnit": {
            "type": "Weight",
            "value": 1,
            "unit": "KG"
          },
          "shopByCategory": "FOOD",
          "action": {
            "type": "PRODUCT_DETAIL",
            "value": "P2001"
          }
        },

        {
          "productId": "P200117",
          "sku": "FOOD-FISH-ADULT-1KG",
          "name": "Premium Fish Food – Adult",
          "imageUrl": "https://firebasestorage.googleapis.com/v0/b/urs-system-interactors-2e9e0.appspot.com/o/aquaPetsZone%2Faquarium-pellet-fish-feed-stock-photo-48323932.webp?alt=media&token=eec44165-db9e-4afe-96d3-40bf94d039c1",
          "price": {
            "originalPrice": 899,
            "sellingPrice": 749,
            "currency": "INR"
          },
          "discount": {
            "percentage": 17,
            "label": "17% OFF"
          },
          "rating": {
            "average": 4.5,
            "count": 342
          },
          "availability": "IN_STOCK",
          "badge": {
            "type": "BEST_SELLER",
            "text": "Top Rated"
          },
          "sellingUnit": {
            "type": "Weight",
            "value": 1,
            "unit": "KG"
          },
          "shopByCategory": "FOOD",
          "action": {
            "type": "PRODUCT_DETAIL",
            "value": "P2001"
          }
        },
        {
          "productId": "P100118",
          "sku": "FISH-GOLD-001",
          "name": "Golden Fish",
          "imageUrl": "https://firebasestorage.googleapis.com/v0/b/urs-system-interactors-2e9e0.appspot.com/o/aquaPetsZone%2FImage%20(Goldfish%20Premium%20Quality).png?alt=media&token=e0acdb2e-b1e0-48d1-b7f2-759542ecb914",
          "price": {
            "originalPrice": 499,
            "sellingPrice": 399,
            "currency": "INR"
          },
          "discount": {
            "percentage": 20,
            "label": "20% OFF"
          },
          "rating": {
            "average": 4.6,
            "count": 124
          },
          "availability": "IN_STOCK",
          "badge": {
            "type": "BEST_SELLER",
            "text": "Best Seller"
          },
          "sellingUnit": {
            "type": "Quantity",
            "value": 2,
            "unit": "PC"
          },
          "shopByCategory": "FISH",
          "action": {
            "type": "PRODUCT_DETAIL",
            "value": "P1001"
          }
        },

        {
          "productId": "P100319",
          "sku": "FISH-GUPPY-003",
          "name": "Guppy Fish",
          "imageUrl": "https://firebasestorage.googleapis.com/v0/b/urs-system-interactors-2e9e0.appspot.com/o/aquaPetsZone%2Fguppy.webp?alt=media&token=8325f94f-eea2-4fcb-829e-892d08c484ff",
          "price": {
            "originalPrice": 599,
            "sellingPrice": 499,
            "currency": "INR"
          },
          "discount": {
            "percentage": 17,
            "label": "17% OFF"
          },
          "rating": {
            "average": 4.7,
            "count": 201
          },
          "availability": "IN_STOCK",
          "badge": {
            "type": "FEATURED",
            "text": "Featured"
          },
          "sellingUnit": {
            "type": "Quantity",
            "value": 10,
            "unit": "PAIR"
          },
          "shopByCategory": "FISH",
          "action": {
            "type": "PRODUCT_DETAIL",
            "value": "P1003"
          }
        },

        {
          "productId": "P200120",
          "sku": "FOOD-FISH-ADULT-1KG",
          "name": "Premium Fish Food – Adult",
          "imageUrl": "https://firebasestorage.googleapis.com/v0/b/urs-system-interactors-2e9e0.appspot.com/o/aquaPetsZone%2Faquarium-pellet-fish-feed-stock-photo-48323932.webp?alt=media&token=eec44165-db9e-4afe-96d3-40bf94d039c1",
          "price": {
            "originalPrice": 899,
            "sellingPrice": 749,
            "currency": "INR"
          },
          "discount": {
            "percentage": 17,
            "label": "17% OFF"
          },
          "rating": {
            "average": 4.5,
            "count": 342
          },
          "availability": "IN_STOCK",
          "badge": {
            "type": "BEST_SELLER",
            "text": "Top Rated"
          },
          "sellingUnit": {
            "type": "Weight",
            "value": 1,
            "unit": "KG"
          },
          "shopByCategory": "FOOD",
          "action": {
            "type": "PRODUCT_DETAIL",
            "value": "P2001"
          }
        },

        {
          "productId": "P200121",
          "sku": "FOOD-FISH-ADULT-1KG",
          "name": "Premium Fish Food – Adult",
          "imageUrl": "https://firebasestorage.googleapis.com/v0/b/urs-system-interactors-2e9e0.appspot.com/o/aquaPetsZone%2Faquarium-pellet-fish-feed-stock-photo-48323932.webp?alt=media&token=eec44165-db9e-4afe-96d3-40bf94d039c1",
          "price": {
            "originalPrice": 899,
            "sellingPrice": 749,
            "currency": "INR"
          },
          "discount": {
            "percentage": 17,
            "label": "17% OFF"
          },
          "rating": {
            "average": 4.5,
            "count": 342
          },
          "availability": "IN_STOCK",
          "badge": {
            "type": "BEST_SELLER",
            "text": "Top Rated"
          },
          "sellingUnit": {
            "type": "Weight",
            "value": 1,
            "unit": "KG"
          },
          "shopByCategory": "FOOD",
          "action": {
            "type": "PRODUCT_DETAIL",
            "value": "P2001"
          }
        }


      ],

      "viewAllEnabled": true,
      "viewAllAction": {
        "type": "CATEGORY",
        "value": "featured"
      },

      "tracking": {
        "impressionId": "imp_home_featured_001",
        "source": "HOME"
      }
    }
    """.trimIndent()

}