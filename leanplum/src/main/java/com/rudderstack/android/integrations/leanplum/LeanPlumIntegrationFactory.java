package com.rudderstack.android.integrations.leanplum;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leanplum.Leanplum;
import com.leanplum.LeanplumActivityHelper;
import com.leanplum.internal.Log;
import com.rudderstack.android.sdk.core.MessageType;
import com.rudderstack.android.sdk.core.RudderClient;
import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderContext;
import com.rudderstack.android.sdk.core.RudderIntegration;
import com.rudderstack.android.sdk.core.RudderLogger;
import com.rudderstack.android.sdk.core.RudderMessage;

import java.util.HashMap;
import java.util.Map;

public class LeanPlumIntegrationFactory extends RudderIntegration<Void> {
    private static final String LEANPLUM_KEY = "Leanplum";
    private boolean sendEvents = false;

    public static RudderIntegration.Factory FACTORY = new Factory() {
        @Override
        public RudderIntegration<?> create(Object settings, RudderClient client, RudderConfig config) {
            return new LeanPlumIntegrationFactory(settings, client, config);
        }

        @Override
        public String key() {
            return LEANPLUM_KEY;
        }
    };

    private LeanPlumIntegrationFactory(@Nullable Object config, @NonNull RudderClient client, @NonNull RudderConfig rudderConfig) {
        final Map<Integer, Integer> logMapping = new HashMap<Integer, Integer>() {
            {
                put(RudderLogger.RudderLogLevel.DEBUG, Log.Level.DEBUG);
                put(RudderLogger.RudderLogLevel.INFO, Log.Level.INFO);
                put(RudderLogger.RudderLogLevel.ERROR, Log.Level.ERROR);
                put(RudderLogger.RudderLogLevel.NONE, Log.Level.OFF);
            }
        };

        Leanplum.setApplicationContext(RudderClient.getApplication());
        if (RudderClient.getApplication() != null) {
            LeanplumActivityHelper.enableLifecycleCallbacks(RudderClient.getApplication());
        }

        Map<String, Object> configMap = (Map<String, Object>) config;
        if (configMap != null) {
            Boolean isDevelop = (Boolean) configMap.get("isDevelop");
            String appId = (String) configMap.get("applicationId");
            String clientKey = (String) configMap.get("clientKey");
            Boolean sendEventsBool = (Boolean) configMap.get("useNativeSDKToSend");
            this.sendEvents = sendEventsBool != null && sendEventsBool;

            if (isDevelop != null && appId != null && clientKey != null) {
                if (isDevelop) {
                    Leanplum.setAppIdForDevelopmentMode(appId, clientKey);
                } else {
                    Leanplum.setAppIdForProductionMode(appId, clientKey);
                }
            }
            if (logMapping.containsKey(rudderConfig.getLogLevel())) {
                Leanplum.setLogLevel(logMapping.get(rudderConfig.getLogLevel()));
            }

            RudderContext context = client.getRudderContext();
            String userId = null;
            if (context != null && context.getTraits() != null) {
                Map<String, Object> traits = context.getTraits();
                if (traits.containsKey("userId")) {
                    userId = (String) traits.get("userId");
                } else if (traits.containsKey("id")) {
                    userId = (String) traits.get("id");
                }
            }
            if (userId != null) {
                Leanplum.start(RudderClient.getApplication(), userId);
            } else {
                Leanplum.start(RudderClient.getApplication());
            }
        }
    }

    private void processEvents(@NonNull RudderMessage message) {
        String eventType = message.getType();
        if (eventType != null) {
            switch (eventType) {
                case MessageType.TRACK:
                    String eventName = message.getEventName();
                    if (eventName != null) {
                        Map<String, Object> properties = message.getProperties();
                        properties = filterProperties(properties);
                        if (eventName.equalsIgnoreCase("Order Completed")) {
                            if (properties != null) {
                                String currency = (String) properties.get("currency");
                                Object revenue = properties.get("revenue");
                                if (revenue instanceof Number) {
                                    Leanplum.trackPurchase(eventName, ((Number) revenue).doubleValue(), currency, properties);
                                }
                                Leanplum.track(eventName, properties);
                            } else {
                                Leanplum.track(eventName);
                            }
                        } else {
                            if (properties != null && properties.containsKey("value")) {
                                Object value = properties.get("value");
                                if (value instanceof Double) {
                                    Double val = (Double) value;
                                    if (val != 0.0D) {
                                        Leanplum.track(eventName, (Double) value, properties);
                                    } else {
                                        Leanplum.track(eventName, properties);
                                    }
                                }
                            } else {
                                Leanplum.track(eventName, properties);
                            }
                        }
                    }
                    break;
                case MessageType.SCREEN:
                    if (message.getEventName() != null) {
                        Leanplum.advanceTo(message.getEventName(), message.getProperties());
                    }
                    break;
                case MessageType.IDENTIFY:
                    Leanplum.setUserId(message.getUserId());
                    Leanplum.setUserAttributes(message.getTraits());
                    break;
                default:
                    RudderLogger.logWarn("Message type is not supported");
            }
        }
    }

    private Map<String, Object> filterProperties(Map<String, Object> properties) {
        if (properties != null) {
            Map<String, Object> filteredProperties = new HashMap<>();
            for (String key : properties.keySet()) {
                Object val = properties.get(key);
                if (val instanceof String ||
                        val instanceof Number ||
                        val instanceof Boolean) {
                    filteredProperties.put(key, val);
                }
            }
            return filteredProperties;
        }
        return null;
    }

    @Override
    public void reset() {
        Leanplum.clearUserContent();
    }

    @Override
    public void dump(RudderMessage element) {
        try {
            if (this.sendEvents && element != null) {
                this.processEvents(element);
            }
        } catch (Exception e) {
            RudderLogger.logError(e);
        }
    }
}
