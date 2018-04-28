(ns eggbankscraper.core)

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

(def extract-banks
  (comp
    (map get-bank-data-url)
    (map get-site-contents)
    (mapcat scrape-raw-banks)
    (map extract-bank-data)))

(defn -main
  [& args]
  (let [states ["AL" "AK" "FL" "VA" "NC" "SC" "AZ" 
                "MI" "MS" "MO" "NE" "DC" "WA" "OR" 
                "CA" "NV" "UT" "ID" "WY" "MT" "NM"
                "TX" "OK" "KS" "CO" "RI" "NY" "CT"
                "VT" "NH" "ME" "MA" "GA" "TN" "KY"
                "OH" "WV" "PA" "NJ" "IL" "IN" "IA"
                "HI" "MD" "DE" "AR" "ND" "SD"]]
    (spit "clinics.txt" (transduce extract-banks conj states))))

