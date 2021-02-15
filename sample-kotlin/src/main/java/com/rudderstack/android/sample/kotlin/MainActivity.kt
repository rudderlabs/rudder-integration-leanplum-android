package com.rudderstack.android.sample.kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rudderlabs.android.sample.kotlin.R
import com.rudderstack.android.sdk.core.RudderProperty
import com.rudderstack.android.sdk.core.RudderTraits
import com.rudderstack.android.sdk.core.ecomm.ECommerceOrder
import com.rudderstack.android.sdk.core.ecomm.ECommerceProduct
import com.rudderstack.android.sdk.core.ecomm.events.OrderCompletedEvent

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MainApplication.rudderClient.screen(localClassName)

        val property = RudderProperty()
        property.put("key_1", "val_1")
        property.put("key_2", "val_2")
        val childProperty = RudderProperty()
        childProperty.put("key_c_1", "val_c_1")
        childProperty.put("key_c_2", "val_c_2")
        property.put("child_key", childProperty)
        MainApplication.rudderClient.track("challenge: applied points", property)
        MainApplication.rudderClient.track("article: viewed")
        MainApplication.rudderClient.identify(
            "test_user_id",
            RudderTraits()
                .putEmail("example@gmail.com")
                .putFirstName("Foo")
                .putLastName("Bar"),
            null
        )
        MainApplication.rudderClient.track("account: created")
        MainApplication.rudderClient.track("account: authenticated")

        val productA = ECommerceProduct.Builder()
            .withProductId("some_product_id_a")
            .withSku("some_product_sku_a")
            .withCurrency("USD")
            .withPrice(2.99f)
            .withName("Some Product Name A")
            .withQuantity(1f)
            .build()

        val productB = ECommerceProduct.Builder()
            .withProductId("some_product_id_b")
            .withSku("some_product_sku_b")
            .withCurrency("USD")
            .withPrice(3.99f)
            .withName("Some Product Name B")
            .withQuantity(1f)
            .build()

        val productC = ECommerceProduct.Builder()
            .withProductId("some_product_id_c")
            .withSku("some_product_sku_c")
            .withCurrency("USD")
            .withPrice(4.99f)
            .withName("Some Product Name C")
            .withQuantity(1f)
            .build()
        val order = ECommerceOrder.Builder()
            .withOrderId("some_order_id")
            .withAffiliation("some_order_affiliation")
            .withCoupon("some_coupon")
            .withCurrency("USD")
            .withDiscount(1.49f)
            .withProducts(productA, productB, productC)
            .withRevenue(10.99f)
            .withShippingCost(2.49f)
            .withTax(1.49f)
            .withTotal(12.99f)
            .withValue(10.49f)
            .build()
        val orderCompletedEvent = OrderCompletedEvent().withOrder(order)
        MainApplication.rudderClient.track(
            orderCompletedEvent.event(),
            orderCompletedEvent.properties()
        )
    }
}
