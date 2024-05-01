(ns diligence.today.web.views.question-maker
    (:require
      [diligence.today.util :as util]
      [diligence.today.web.controllers.file :as file]
      [diligence.today.web.controllers.iam :as iam]
      [diligence.today.web.controllers.project :as project]
      [diligence.today.web.controllers.question :as question]
      [diligence.today.web.htmx :refer [page-htmx defcomponent defcomponent-user]]
      [diligence.today.web.views.common :as common :refer [href-viewer]]
      [diligence.today.web.views.components :as components]
      [diligence.today.web.views.dropdown :as dropdown]
      [diligence.today.web.views.icons :as icons]
      [simpleui.core :as simpleui]
      [simpleui.response :as response]))

(defcomponent ^:endpoint question-edit [req ^:long-option question_id question]
  (if (simpleui/post? req)
    (when-let [question (some-> question .trim not-empty)]
      (if question_id
        (iam/when-authorized
         (question/update-question req question_id question)
         response/hx-refresh)
        (iam/when-authorized
         (try
           (question/add-question req project_id question)
           response/hx-refresh
           (catch clojure.lang.ExceptionInfo e
             (if (util/uniqueness-violation? e)
               [:div#duplicate-warning.my-2 (components/warning "Name taken")]
               (throw e)))))))
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

(defn project-ro [project-name]
  [:form {:class "flex items-center"
          :hx-get "project-edit"}
   (components/hiddensm project-name)
   [:div.my-6.mr-4.text-gray-500.text-4xl project-name]
   [:input {:class "bg-clj-blue p-1.5 rounded-lg text-white w-24 cursor-pointer"
            :type "submit"
            :value "Edit"}]])

(defn file-selector [req project_id]
  (list
   [:div.mb-2 (components/button-label "add-file" "Add file to project")
    [:input#add-file {:class "hidden"
                      :type "file"
                      :name "file"
                      :accept "application/pdf"
                      :hx-post "question-maker"
                      :hx-encoding "multipart/form-data"}]]
   [:div.flex.items-center
    (for [{:keys [file_id]} (file/get-files req project_id)]
      [:a {:href (href-viewer project_id)
           :target "_blank"}
       [:img {:class "w-64"
              :src (str "/api/thumbnail/" file_id)}]])]))

(defcomponent-user ^:endpoint question-maker [req file]
  project-edit
  (if (simpleui/post? req)
    (iam/when-authorized
     (file/copy-file req project_id file)
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
       [:div {:class "w-3/4 border rounded-lg mx-auto"}
        [:table.w-full
         [:tbody
          (for [{:keys [question_id question]} questions]
            (question-ro req question_id question))]]
        [:hr.my-4.border]
        [:div#duplicate-warning]
        (question-edit req nil nil)
        [:hr.my-4.border]
        (file-selector req project_id)]])))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (if (-> req :session :user_id)
       (page-htmx
        {:hyperscript? true}
        (-> req (assoc :query-fn query-fn) question-maker))
       (response/redirect "/")))))
