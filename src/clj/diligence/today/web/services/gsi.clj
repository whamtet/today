(ns diligence.today.web.services.gsi
    (:require
      [clojure.walk :as walk]
      [diligence.today.util :refer [defm]])
    (:import
      [com.google.api.client.googleapis.auth.oauth2 GoogleIdTokenVerifier GoogleIdTokenVerifier$Builder]
      [com.google.api.client.googleapis.javanet GoogleNetHttpTransport]
      [com.google.api.client.json.gson GsonFactory]
      [java.util Collection]))

(defm verifier []
  (let [builder (GoogleIdTokenVerifier$Builder. (GoogleNetHttpTransport/newTrustedTransport) (GsonFactory.))
        ^Collection audience (list (System/getenv "GSI_CLIENT"))]
    (.setAudience builder audience)
    (.build builder)))

(defn req->user-info [{{{cookie-token :value} "g_csrf_token"} :cookies
                       {:keys [g_csrf_token ^String credential]} :params}]
  (assert (= cookie-token g_csrf_token))
  (let [^GoogleIdTokenVerifier v (verifier)]
    (when-let [token (.verify v credential)]
      (->> token .getPayload (into {}) walk/keywordize-keys))))
