Rails.application.routes.draw do
  # For details on the DSL available within this file, see http://guides.rubyonrails.org/routing.html
  get 'platforms/import'
  get 'nodes/copy'

  resources :groups
  resources :accounts
  resources :platforms
  resources :nodes
  resources :node_configs
  resources :tags
  resources :tag_nodes
  resources :platform, only: [:index, :new, :create, :destroy]

  root :to => 'nodes#index'
end
