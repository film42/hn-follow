(ns hn-follow.core.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [hn-follow.core.handler :refer :all]
            [cheshire.core :refer :all]))

(deftest hn-follow-router  
  (testing "not-found route"
    (let [response (app (mock/request :get "/invalidasdasdfasdfsfd"))]
      (is (= (:status response) 404))))

  (testing "home page route"
    (let [response (app (mock/request :get "/"))]
      (is (not (nil? (re-find #"HN Follow" (:body response)))))))

  (testing "can add a user"
    (let [response
          (app (mock/request :post "/api/u" (mock/body nil "")))]
      (is (not (nil? (re-find #"No username" (:body response)) )))))

  (testing "can get a user"
    (let [response (app (mock/request :get "/api/a/abc123nononono"))
          json (parse-string (response :body) true)]
      (is (contains? json :username))
      (is (= "abc123nononono" (json :username)))
      (is (contains? json :follow))))

  (testing "can get interactions"
    (let [response (app (mock/request :get "/api/i/abc123nononono"))
          json (parse-string (response :body) true)]
      (is (contains? json :interactions))
      (is (empty? (json :interactions))))))
