Rails.application.routes.draw do
  get 'platforms/import'
  get 'nodes/copy'

  # For details on the DSL available within this file, see http://guides.rubyonrails.org/routing.html
  resources :groups
  resources :accounts
  resources :platforms
  resources :nodes
  resources :node_configs
  resources :tags
  resources :tag_nodes
end
