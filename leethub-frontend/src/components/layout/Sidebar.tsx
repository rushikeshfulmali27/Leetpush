import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../contexts/useAuth';
import { 
  HomeIcon, 
  CodeBracketIcon, 
  MagnifyingGlassIcon, 
  DocumentTextIcon, 
  Cog6ToothIcon 
} from '@heroicons/react/24/outline';

export const Sidebar: React.FC = () => {
  const { user } = useAuth();

  const navigation = [
    { name: 'Dashboard', href: '/dashboard', icon: HomeIcon },
    { name: 'Problems', href: '/problems', icon: CodeBracketIcon },
    { name: 'Search', href: '/search', icon: MagnifyingGlassIcon },
    { name: 'Notes', href: '/notes', icon: DocumentTextIcon },
  ];

  return (
    <div className="flex h-full w-64 flex-col border-r border-gray-800 bg-gray-900">
      <div className="flex h-16 shrink-0 items-center px-6">
        <h1 className="text-2xl font-bold tracking-tight text-white">
          LeetHub <span className="text-emerald-500">AI</span>
        </h1>
      </div>
      
      <nav className="flex flex-1 flex-col overflow-y-auto px-4 py-4">
        <ul role="list" className="flex flex-1 flex-col gap-y-7">
          <li>
            <ul role="list" className="-mx-2 space-y-2">
              {navigation.map((item) => (
                <li key={item.name}>
                  <NavLink
                    to={item.href}
                    className={({ isActive }) =>
                      `group flex gap-x-3 rounded-md p-2 text-sm font-semibold leading-6 transition-colors ${
                        isActive
                          ? 'bg-gray-800 text-emerald-400'
                          : 'text-gray-400 hover:bg-gray-800 hover:text-white'
                      }`
                    }
                  >
                    <item.icon className="h-6 w-6 shrink-0" aria-hidden="true" />
                    {item.name}
                  </NavLink>
                </li>
              ))}
            </ul>
          </li>
          
          <li className="mt-auto">
            <NavLink
              to="/settings"
              className={({ isActive }) =>
                `group -mx-2 flex gap-x-3 rounded-md p-2 text-sm font-semibold leading-6 transition-colors ${
                  isActive
                    ? 'bg-gray-800 text-emerald-400'
                    : 'text-gray-400 hover:bg-gray-800 hover:text-white'
                }`
              }
            >
              <Cog6ToothIcon className="h-6 w-6 shrink-0" aria-hidden="true" />
              Settings
            </NavLink>
          </li>
        </ul>
      </nav>
      
      {user && (
        <div className="flex shrink-0 items-center gap-x-4 border-t border-gray-800 bg-gray-900 p-4">
          <img
            className="h-10 w-10 rounded-full bg-gray-800"
            src={user.avatarUrl || `https://ui-avatars.com/api/?name=${user.username}&background=0D8ABC&color=fff`}
            alt=""
          />
          <div className="flex flex-col">
            <span className="text-sm font-semibold text-white">{user.username}</span>
            <span className="text-xs text-gray-400">Pro Plan</span>
          </div>
        </div>
      )}
    </div>
  );
};
