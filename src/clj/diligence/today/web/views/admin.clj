(ns diligence.today.web.views.admin
    (:require
      [clojure.string :as string]
      [diligence.today.env :refer [dev?]]
      [diligence.today.util :as util :refer [format-json]]
      [diligence.today.web.controllers.file :as file]
      [diligence.today.web.controllers.project :as project]
      [diligence.today.web.controllers.question :as question]
      [diligence.today.web.htmx :refer [page-htmx defcomponent defcomponent-user]]
      [diligence.today.web.views.common :as common]
      [diligence.today.web.views.components :as components]
      [diligence.today.web.views.dropdown :as dropdown]
      [diligence.today.web.views.icons :as icons]
      [simpleui.core :as simpleui :refer [with-commands]]
      [simpleui.response :as response]
      [simpleui.rt :as rt]))

(defcomponent ^:endpoint project-edit [req project-name]
  (if (simpleui/post? req)
    (when-let [project-name (some-> project-name .trim not-empty)]
      (do
       (project/update-project req project_id project-name)
       response/hx-refresh))
    [:form {:class "flex items-center"
            :hx-post "project-edit"}
     [:input {:class "p-2 text-4xl my-6 mr-4"
              :name "project-name"
              :value project-name
              :hx-post "project-edit"
              :placeholder "Project name..."}]
     [:input {:class "bg-clj-blue p-1.5 rounded-lg text-white w-24 cursor-pointer"
              :type "submit"
              :value "Save"}]]))

(defn project-ro [project-name]
  [:form {:class "flex items-center"
          :hx-get "project-edit"}
   (components/hiddensm project-name)
   [:div.my-6.mr-4.text-gray-500.text-4xl project-name]
   [:input {:class "bg-clj-blue p-1.5 rounded-lg text-white w-24 cursor-pointer"
            :type "submit"
            :value "Edit"}]])

(defcomponent ^:endpoint question-editor [req
                                          ^:long question_id
                                          question
                                          command]
  (with-commands req
                 (when update? (question/update-question req question_id question))
                 (cond
                  delete?
                  (do
                   (question/delete-question req question_id)
                   response/hx-refresh)
                  edit?
                  [:form {:class "p-2 flex items-center justify-between"
                          :hx-post "question-editor:update"
                          :hx-vals {:question_id question_id}}
                   [:input {:class "w-full p-1 border mr-2"
                            :name "question"
                            :value question
                            :required true}]
                   (components/submit "Save")]
                  :else
                  [:div {:class "p-2 flex items-center justify-between"
                         :hx-target "this"}
                   [:div question]
                   [:div.flex.items-center
                    [:div {:class "mr-2"
                           :hx-get "question-editor:edit"
                           :hx-vals {:question_id question_id
                                     :question question}}
                     (components/button "Edit")]
                    [:span {:class "opacity-50 cursor-pointer"
                            :hx-delete "question-editor:delete"
                            :hx-confirm "Delete question? This will wipe the answer as well."
                            :hx-vals {:question_id question_id}}
                     icons/trash]]])))

[:div {:class "w-2/3"}]
(defcomponent ^:endpoint new-modal [req ^:long-option section_id]
  (if top-level?
    (components/modal "w-2/3"
                      [:div.p-2
                       [:h3.mb-2 (if section_id "New Question" "New Section")]
                       [:form {:class "flex"
                               :hx-post "admin"
                               :hx-vals {:command (if section_id "new-question" "new-section")}}
                        (when section_id [:input {:type "hidden" :name "section_id" :value section_id}])
                        [:input {:class "w-full border rounded-md p-2 mr-2"
                                 :name (if section_id "question" "section")
                                 :placeholder (if section_id "New Question Name..." "New Section Name...")
                                 :list (if section_id "suggestions" "suggestions-section")
                                 :required true}]
                        (components/submit "Create")]])
    [:div {:hx-get "new-modal"
           :hx-vals {:section_id section_id}
           :hx-target "#modal"}
     (components/button
      (if section_id "New Question" "New Section"))]))

(defcomponent ^:endpoint section-editor [req
                                         ^:boolean last
                                         ^:long ordering
                                         ^:long section_id
                                         section
                                         command]
  (with-commands req
                 section-editor
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
                     (when dev?
                           [:script
                            (format-json "newQuestion = () => hxPost('admin:new-question', %s);"
                                         {:section_id section_id
                                          :question "Question1"})])
                     (new-modal req section_id)]])))

(defcomponent ^:endpoint section-section [req
                                          last?
                                          [base-section questions]
                                          section_id]
  (if (simpleui/delete? req)
    (do
     (question/delete-section req section_id)
     response/hx-refresh)
    [:div.pb-12
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
     [:hr.border.my-2]
     (rt/map-indexed question-editor req questions)]))

(defcomponent-user ^:endpoint admin [req
                                     ^:prompt section
                                     ^:prompt question
                                     ^:long section_id
                                     ^:long mid
                                     command]
  project-edit
  (case command
        "new-section"
        (do
         (question/insert-section req project_id section)
         response/hx-refresh)
        "new-question"
        (do
         (question/add-question req project_id section_id question)
         response/hx-refresh)
        "move"
        (do
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
            (common/main-dropdown first_name project_id project-name session)]
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
            (when dev?
                  [:script
                   (format-json "newSection = () => hxPost('admin:new-section', %s);"
                                {:section "Section1"})])
            (new-modal req nil)]])))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:hyperscript? true}
      (-> req (assoc :query-fn query-fn) admin)))))
