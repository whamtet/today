(ns diligence.today.web.views.admin-file
    (:require
      [clojure.string :as string]
      [diligence.today.env :as env]
      [diligence.today.util :as util :refer [format-js]]
      [diligence.today.web.controllers.file :as file]
      [diligence.today.web.controllers.iam :as iam]
      [diligence.today.web.controllers.question.migrate :as migrate]
      [diligence.today.web.controllers.project :as project]
      [diligence.today.web.htmx :refer [page-htmx defcomponent-user]]
      [diligence.today.web.views.common :as common]
      [diligence.today.web.views.components :as components]
      [simpleui.core :as simpleui]
      [simpleui.response :as response]))

(def file-accept "application/pdf")
(defn file-selector [req project_id]
  (let [files (file/get-files req project_id)]
    (list
     [:div.flex.items-center
      (components/button-label "add-file" "Add file to project")
      [:input#add-file {:class "hidden"
                        :type "file"
                        :name "file"
                        :accept file-accept
                        :hx-post "question-maker"
                        :hx-encoding "multipart/form-data"}]]
     [:hr.border.my-2]
     (when (not-empty files)
       [:p.my-2.text-gray-700 "Click on a file to update it"])
     [:div.flex.items-center
      (for [{:keys [file_id filename]} files]
        (let [filename-disp (string/replace filename #"\d+.pdf" "pdf")]
          (list
           [:input {:class "hidden"
                    :id (format-js "f{file_id}")
                    :type "file"
                    :name "file"
                    :accept file-accept
                    :hx-post "question-maker"
                    :hx-encoding "multipart/form-data"
                    :hx-vals {:old-filename filename-disp}}]
           [:div {:class "mt-2 cursor-pointer"
                  :onclick (format-js "updateFile('{filename-disp}', 'f{file_id}')")}
            [:div.text-center filename-disp]
            [:img {:class "w-64"
                   :src (format "/api/thumbnail/%s/0" file_id)}]])))])))

(defcomponent-user ^:endpoint question-maker [req file old-filename]
  (if (simpleui/post? req)
    (iam/when-authorized
     (if-let [file_id (migrate/migrate-file req project_id file old-filename)]
       (response/hx-redirect (common/href-viewer {:migrate true
                                                  :file_id file_id
                                                  :project_id project_id
                                                  :file (env/host "/api/file/" file_id)}))
       response/hx-refresh))
    (let [{project-name :name} (project/get-project-by-id req project_id)]
      [:div {:_ "on click add .hidden to .drop"}
       ;; header row
       [:div {:class "flex justify-center"}
        [:a.absolute.left-1.top-1 {:href "/"}
         [:img.w-16.m-2 {:src "/icon.png"}]]
        [:div.my-6.mr-4.text-gray-500.text-4xl project-name]
        (common/main-dropdown first_name project_id)]
       [:div {:class "w-3/4 border rounded-lg mx-auto"}
        [:div.p-2
         (file-selector req project_id)]]])))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:hyperscript? true
       :js ["/file.js"]}
      (-> req (assoc :query-fn query-fn) question-maker)))))
