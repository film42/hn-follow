(ns hn-follow.core.api-test
  (:require [clojure.test :refer :all]
            [ hn-follow.core.api :refer :all]))

(deftest api-basic
  (testing
      (is (= base-url "https://hacker-news.firebaseio.com/v0/"))))

(deftest can-get-user
  (testing
      (let [user (get-user "film42")
            bad-user (get-user "laksdjFERERFERFerfkljerl")]
        (is (nil? bad-user))
        (is (map? user))
        (is (contains? user "about"))
        (is (contains? user "karma"))
        (is (contains? user "id"))
        (is (= (user "id") "film42")))))

(deftest can-get-item
  (testing
      (let [item (get-item "8561849")]
        (is (map? item))
        (is (contains? item "id"))
        (is (contains? item "text"))
        (is (contains? item "time"))
        (is (contains? item "by")))))

(deftest can-get-interaction-tree
  (testing
      (let [user (get-user "film42")
            interactions (interaction-tree user 1)]
        (is (sequential? interactions))
        (is (= (count interactions) 1))
        (is (contains? (first interactions) :tree))
        (is (contains? (first ((first interactions) :tree)) "id")))))
