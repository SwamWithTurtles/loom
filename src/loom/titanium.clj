(ns ^{:doc "Defines an interface between Titanium and loom."
      :author "Aysylu"}
  loom.titanium
  (:require [clojure.set :as set])
  (:require [clojurewerkz.titanium.graph :as tg])
  (:require [clojurewerkz.titanium.elements :as te])
  (:require [clojurewerkz.titanium.edges :as edges])
  (:require [clojurewerkz.titanium.vertices :as nodes])
  (:require loom.io)
  (:use [loom.graph]))

(defn titanium->loom
  "Converts titanium graph into loom representation"
  ([titanium-graph &
    {:keys [node-fn edge-fn weight-fn]
     :or {node-fn (nodes/get-all-vertices)
          edge-fn (map (juxt edges/tail-vertex
                             edges/head-vertex)
                       (edges/get-all-edges))
          weight-fn (constantly 1)}}]
   (let [nodes-set (set node-fn)
         edges-set (set edge-fn)]
   (reify
     Graph
     (nodes [_] nodes-set)
     (edges [_] edges-set)
     (has-node? [g node] (contains? (nodes g) node))
     (has-edge? [g n1 n2] (contains? (edges g) [n1 n2]))
     (neighbors [g] (partial neighbors g))
     (neighbors [g node] (filter (nodes g) (seq (nodes/connected-out-vertices node))))
     (degree [g node] (count (neighbors g node)))
     Digraph
     (incoming [g] (partial incoming g))
     (incoming [g node] (filter (nodes g) (seq (nodes/connected-in-vertices node))))
     (in-degree [g node] (count (incoming g node)))
     WeightedGraph
     (weight [g] (partial weight g))
     (weight [g n1 n2] (weight-fn n1 n2))))))

(defn view [g]
  (loom.io/view (if (satisfies? Graph g)
                  g
                  (titanium->loom g))
                :node-label nodes/to-map
                :edge-label
                (fn [n1 n2]
                  (mapv edges/to-map
                        (edges/edges-between n1 n2)))))
