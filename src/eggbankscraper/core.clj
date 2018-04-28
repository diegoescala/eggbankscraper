(ns eggbankscraper.core
  (:require [clj-http.client :as http]
            [clj-http.util :as util]
            [clojure.data.json :as json]))

(defn extract-bank-data
  [bank-html]
  (let [namestr (clojure.string/replace bank-html #"<h2 class=\"title\"><a href.*?>(.*?)</a>.*" "$1")
        address (clojure.string/replace bank-html #".*?streetAddress\">(.*?)<.*" "$1")
        city (clojure.string/replace bank-html #".*?addressLocality\">(.*?)<.*" "$1")
        state (clojure.string/replace bank-html #".*?addressRegion\">(.*?)<.*" "$1")
        zipcode (clojure.string/replace bank-html #".*?postalCode\">(.*?)<.*" "$1")
        latitude (clojure.string/replace bank-html #".*?latitude\" content=\"(.*?)\".*" "$1")
        longitude (clojure.string/replace bank-html #".*?longitude\" content=\"(.*?)\".*" "$1")]
    {:name namestr :address address :city city :state state :zipcode zipcode :latitude latitude :longitude longitude}))

(defn scrape-raw-banks
  [html]
  (re-seq #"<h2 class=\"title\">.*?</div></div></div></div>" (clojure.string/replace html #"\n" " ")))

(defn get-site-contents
  [url]
  (slurp url))

(defn get-bank-data-url
  [state]
  (let [base-url "https://www.fertilityauthority.com/clinics/bystate/"]
    (str base-url state)))

(defn add-google-place-id 
  [business]
  (let [search-string (util/url-encode (str (:name business) " " (:address business)))]
    (let [base-search-url "https://maps.googleapis.com/maps/api/place/textsearch/json?query="]
      (let [full-url (str base-search-url search-string "&key=AIzaSyBsEZzQqUknbmCLN1cXTW59O9NDZ6kdgUM")]
        (let [response (json/read-str (slurp full-url))]
          (let [results (get response "results")]
            (let [place-id (get (first results) "place_id")]
              (assoc business :place-id place-id))))))))

(def extract-banks
  (comp
    (map get-bank-data-url)
    (map get-site-contents)
    (mapcat scrape-raw-banks)
    (map extract-bank-data)
    ;(map add-google-place-id)
    ))

(def add-phone-numbers
  nil)

(defn scrape-fertility-site
  []
  (let [states ["AL" "AK" "FL" "VA" "NC" "SC" "AZ" 
                "MI" "MS" "MO" "NE" "DC" "WA" "OR" 
                "CA" "NV" "UT" "ID" "WY" "MT" "NM"
                "TX" "OK" "KS" "CO" "RI" "NY" "CT"
                "VT" "NH" "ME" "MA" "GA" "TN" "KY"
                "OH" "WV" "PA" "NJ" "IL" "IN" "IA"
                "HI" "MD" "DE" "AR" "ND" "SD" "WI"
                "MN"]]
    (spit "clinics.txt" (prn-str (transduce extract-banks conj states)))))


(defn -main
  [& args]
  ;(scrape-fertility-site))
;  (let [clinics (read-string (slurp "clinics.txt"))]
;    (spit "clinics-no-places.txt" (prn-str (filter #(= (:place-id %) nil) clinics)))))
  (let [no-places (read-string (slurp "clinics-no-places.txt"))]
    (count no-places)))
    
