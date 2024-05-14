(ns diligence.today.web.views.admin
    (:require
      [clojure.string :as string]
      [diligence.today.util :as util]
      [diligence.today.web.controllers.file :as file]
      [diligence.today.web.controllers.iam :as iam]
      [diligence.today.web.controllers.project :as project]
      [diligence.today.web.controllers.question :as question]
      [diligence.today.web.htmx :refer [page-htmx defcomponent defcomponent-user]]
      [diligence.today.web.views.common :as common]
      [diligence.today.web.views.components :as components]
      [diligence.today.web.views.dropdown :as dropdown]
      [diligence.today.web.views.icons :as icons]
      [simpleui.core :as simpleui]
      [simpleui.response :as response]))

(defcomponent ^:endpoint question-edit [req ^:long-option question_id question]
  (if (simpleui/post? req)
    (iam/when-authorized
     (when-let [question (some-> question .trim not-empty)]
       (if (question/get-question-text req project_id question)
         [:div#duplicate-warning.my-2 (components/warning "Name taken")]
         (do
           (if question_id
             (question/update-question req question_id question)
             (question/add-question req project_id question))
           response/hx-refresh))))
    [:tr
     [:td
      [:input {:class "w-full p-2 form-select"
               :id (str "qe" question_id)
               :name "question"
               :value question
               :hx-post "question-edit"
               :placeholder "New question..."
               :hx-vals {:question_id question_id}
               :hx-target "#duplicate-warning"
               :list "suggestions"}]]
     (when question_id
           [:td
            [:span {:class "ml-2"
                    :hx-post "question-edit"
                    :hx-vals {:question_id question_id}
                    :hx-target "#duplicate-warning"
                    :hx-include (str "#qe" question_id)}
             (components/button "Update")]])]))

(defcomponent ^:endpoint project-edit [req project-name]
  (if (simpleui/post? req)
    (when-let [project-name (some-> project-name .trim not-empty)]
      (iam/when-authorized
       (project/update-project req project_id project-name)
       response/hx-refresh))
    [:form {:hx-post "project-edit"}
     [:input {:class "p-2"
              :name "project-name"
              :value project-name
              :hx-post "project-edit"
              :placeholder "Project name..."}]
     [:input {:class "bg-clj-blue p-1.5 rounded-lg text-white w-24"
              :type "submit"
              :value "Save"}]]))

(defcomponent ^:endpoint question-ro [req question_id question]
  (if (simpleui/delete? req)
    (iam/when-authorized
     (question/delete-question req question_id)
     response/hx-refresh)
    [:tr {:hx-target "this"}
     [:td.p-2 question]
     [:td.p-2
      [:div.flex.items-center
       [:span {:class "mr-2"
               :hx-get "question-edit"
               :hx-vals {:question_id question_id :question question}}
        (components/button "Edit Question")]
       [:span {:class "opacity-50 cursor-pointer"
               :hx-delete "question-ro"
               :hx-confirm "Delete question? Cannot be undone."
               :hx-vals {:question_id question_id}}
        icons/trash]]]]))

(defn command-pair [command]
  [(symbol (str command "?"))
   `(= ~'command ~(str command))])
(defmacro with-commands [commands & body]
  `(let [~@(mapcat command-pair commands)]
    ~@body))

(defcomponent ^:endpoint section-editor [req
                                         ^:boolean last
                                         ^:long ordering
                                         ^:long section_id
                                         section
                                         command]
  (with-commands [edit update]
                 (when update? (question/update-section req section_id section))
                 (if edit?
                   [:form {:class "flex items-center justify-between w-full"
                           :hx-post "section-editor:update"
                           :hx-vals {:last last
                                     :ordering ordering
                                     :section_id section_id}}
                    [:input {:class "w-full p-1 border mr-2 text-xl"
                             :name "section"
                             :value section
                             :required true}]
                    (components/submit "Save")]
                   [:div {:class "flex items-center justify-between w-full"
                          :hx-target "this"}
                    [:div.text-xl section]
                    [:div.flex
                     (when (pos? ordering)
                           [:div {:class "mr-2"
                                  :hx-post "admin:move"
                                  :hx-vals {:mid (dec ordering)}}
                            (components/button "↑")])
                     (when-not last
                               [:div {:class "mr-2"
                                      :hx-post "admin:move"
                                      :hx-vals {:mid (inc ordering)}}
                                (components/button "↓")])
                     [:div {:class "mr-2"
                            :hx-get "section-editor:edit"
                            :hx-vals {:last last
                                      :ordering ordering
                                      :section_id section_id
                                      :section section}}
                      (components/button "Edit")]
                     [:div {:class ""
                            :hx-post "admin:new-question"
                            :hx-prompt "New question name"
                            :hx-vals {:section_id section_id}}
                      (components/button "New Question")]]])))

(defcomponent ^:endpoint section-section [req
                                          last?
                                          [base-section questions]
                                          section_id]
  (if (simpleui/delete? req)
    (iam/when-authorized
     (question/delete-section req section_id)
     response/hx-refresh)
    [:div
     [:div {:class "text-gray-800 p-2 flex items-center justify-between"}
      (section-editor req
                      last?
                      (:ordering base-section)
                      (:section_id base-section)
                      (:section base-section)
                      nil)
      (when (empty? questions)
            [:div {:class "ml-2 cursor-pointer"
                   :hx-delete "section-section"
                   :hx-vals {:section_id (:section_id base-section)}
                   :hx-confirm "Remove section?"}
             icons/trash])]
     [:table.w-full
      [:tbody
       (for [{:keys [question_id question]} questions]
         (question-ro req question_id question))]]]))

(defn project-ro [project-name]
  [:form {:class "flex items-center"
          :hx-get "project-edit"}
   (components/hiddensm project-name)
   [:div.my-6.mr-4.text-gray-500.text-4xl project-name]
   [:input {:class "bg-clj-blue p-1.5 rounded-lg text-white w-24 cursor-pointer"
            :type "submit"
            :value "Edit"}]])

(defcomponent-user ^:endpoint admin [req ^:long section_id ^:long mid command]
  project-edit
  (case command
        "new-section"
        (iam/when-authorized
         (question/insert-section req project_id (get-in req [:headers "hx-prompt"]))
         response/hx-refresh)
        "new-question"
        (iam/when-authorized
         (question/add-question req project_id section_id (get-in req [:headers "hx-prompt"]))
         response/hx-refresh)
        "move"
        (iam/when-authorized
         (question/move-section req mid)
         response/hx-refresh)
        (let [questions (question/get-questions req project_id)
              {project-name :name} (project/get-project-by-id req project_id)]
          [:div {:_ "on click add .hidden to .drop"}
           ;; header row
           [:div {:class "flex justify-center"}
            [:a.absolute.left-1.top-1 {:href "/"}
             [:img.w-16.m-2 {:src "/icon.png"}]]
            (project-ro project-name)
            (common/main-dropdown first_name project_id)]
           [:datalist#suggestions
            (map
             #(vector :option {:value %})
             (question/get-suggestions req project_id))]
           [:datalist#suggestions-section
            (map
             #(vector :option {:value %})
             (question/get-suggestions-section req project_id))]
           [:div {:class "w-3/4 border rounded-md mx-auto p-1"}
            [:div#duplicate-warning]
            (util/map-last #(section-section req %1 %2 nil) questions)
            [:hr.my-4.border]
            [:div {:hx-post "admin:new-section"
                   :hx-prompt "New section name"}
             (components/button "Add Section")]]])))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:hyperscript? true}
      (-> req (assoc :query-fn query-fn) admin)))))
