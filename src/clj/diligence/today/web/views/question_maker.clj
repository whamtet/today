(ns diligence.today.web.views.question-maker
    (:require
      [diligence.today.web.controllers.iam :as iam]
      [diligence.today.web.controllers.project :as project]
      [diligence.today.web.controllers.question :as question]
      [diligence.today.web.htmx :refer [page-htmx defcomponent defcomponent-user]]
      [diligence.today.web.views.common :as common]
      [diligence.today.web.views.components :as components]
      [diligence.today.web.views.dropdown :as dropdown]
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
         (question/add-question req project_id question)
         response/hx-refresh)))
    [:div
     [:input {:class "w-full p-2 form-select"
              :name "question"
              :value question
              :hx-post "question-edit"
              :placeholder "New question..."
              :hx-vals {:question_id question_id}
              :list "suggestions"}]]))

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

(defn question-ro [question_id question]
  [:form {:class "flex items-center my-2"
          :hx-get "question-edit"}
   (components/hiddensm question_id question)
   [:div.min-w-72.mx-2 question]
   [:input {:class "bg-clj-blue p-1.5 rounded-lg text-white w-24"
            :type "submit"
            :value "Edit"}]])

(defn project-ro [project-name]
  [:form {:class "flex items-center"
          :hx-get "project-edit"}
   (components/hiddensm project-name)
   [:div.my-6.mr-4.text-gray-500.text-4xl project-name]
   [:input {:class "bg-clj-blue p-1.5 rounded-lg text-white w-24"
            :type "submit"
            :value "Edit"}]])

(defcomponent-user ^:endpoint question-maker [req]
  project-edit
  (let [questions (question/get-questions req project_id)
        {project-name :name} (project/get-project-by-id req project_id)]
    [:div {:_ "on click add .hidden to .drop"}
     ;; header row
     [:div {:class "flex justify-center"}
      [:a.absolute.left-1.top-1 {:href "/"}
       [:img.w-16.m-2 {:src "/icon.png"}]]
      (project-ro project-name)
      (common/main-dropdown first_name)]
     [:datalist#suggestions
      (map
       #(vector :option {:value %})
       question/suggestions)]
     [:div {:class "w-3/4 border rounded-lg mx-auto"}
      (for [{:keys [question_id question]} questions]
        (question-ro question_id question))
      [:hr.mt-4.border]
      (question-edit req nil nil)]]))

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
